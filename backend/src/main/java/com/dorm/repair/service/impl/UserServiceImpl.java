package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.UserService;
import com.dorm.repair.utils.PasswordUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Resource
    private PasswordUtils passwordUtils;
    
    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return baseMapper.selectOne(wrapper);
    }
    
    @Override
    public User register(RegisterDTO dto) {
        // 修理工注册时自动生成工号
        String username = dto.getUsername();
        if ("REPAIR".equals(dto.getRole())) {
            username = generateRepairmanCode();
        }
        
        User existing = findByUsername(username);
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : "STUDENT");
        user.setStatus(1);
        user.setBuilding(dto.getBuilding());
        user.setDormNumber(dto.getDormNumber());
        user.setCreateTime(LocalDateTime.now());
        
        baseMapper.insert(user);
        return user;
    }
    
    /**
     * 生成修理工工号，格式：WX + 年月日 + 4位序号
     * 例如：WX202401010001
     */
    private String generateRepairmanCode() {
        String prefix = "WX";
        String dateStr = LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        );
        
        // 查询当天最大序号
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.likeRight("username", prefix + dateStr);
        wrapper.eq("role", "REPAIR");
        wrapper.orderByDesc("username");
        wrapper.last("LIMIT 1");
        
        User lastUser = baseMapper.selectOne(wrapper);
        
        int sequence = 1;
        if (lastUser != null) {
            String lastCode = lastUser.getUsername();
            try {
                String seqStr = lastCode.substring(prefix.length() + dateStr.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (Exception e) {
                sequence = 1;
            }
        }
        
        return prefix + dateStr + String.format("%04d", sequence);
    }
    
    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        
        if (!passwordUtils.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
    
    @Override
    public List<User> findByRole(String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", role);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordUtils.encode(newPassword));
        updateById(user);
    }
    
    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        updateById(user);
    }
}
