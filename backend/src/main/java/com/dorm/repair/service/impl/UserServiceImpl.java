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
    
    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectOne(wrapper);
    }
    
    @Override
    public User register(RegisterDTO dto) {
        // 修理工注册时自动生成工号
        String username = dto.getUsername();
        if ("REPAIR".equals(dto.getRole())) {
            username = generateRepairmanCode();
        }
        
        // 先检查该用户名是否存在（无论是否删除）
        int count = baseMapper.countByUsername(username);
        if (count > 0) {
            // 用户名存在，尝试恢复（不管is_deleted值是什么）
            int updated = baseMapper.restoreUserByUsername(
                username,
                PasswordUtils.encode(dto.getPassword()),
                dto.getRealName(),
                dto.getPhone(),
                dto.getRole() != null ? dto.getRole() : "STUDENT",
                dto.getBuilding(),
                dto.getDormNumber()
            );
            
            if (updated > 0) {
                // 恢复成功，更新 specialtyIds（如果是修理工）
                if ("REPAIR".equals(dto.getRole()) && dto.getSpecialtyIds() != null) {
                    User restoredUser = findByUsername(username);
                    restoredUser.setSpecialtyIds(dto.getSpecialtyIds());
                    baseMapper.updateById(restoredUser);
                    return restoredUser;
                }
                // 查询并返回用户
                return findByUsername(username);
            }
            
            // 恢复失败，说明用户已存在且未删除
            throw new RuntimeException("用户名已存在");
        }
        
        // 用户名不存在，执行新增
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : "STUDENT");
        user.setStatus(1);
        user.setBuilding(dto.getBuilding());
        user.setDormNumber(dto.getDormNumber());
        // 修理工设置擅长类型
        if ("REPAIR".equals(dto.getRole()) && dto.getSpecialtyIds() != null) {
            user.setSpecialtyIds(dto.getSpecialtyIds());
        }
        user.setCreateTime(LocalDateTime.now());
        
        baseMapper.insert(user);
        return user;
    }
    
    /**
     * 生成修理工工号，格式：4位数字，从0001开始递增
     * 例如：0001, 0002, 0003...
     */
    private String generateRepairmanCode() {
        // 查询所有修理工工号（4位纯数字格式）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "REPAIR");
        wrapper.eq("is_deleted", 0);
        
        List<User> repairmen = baseMapper.selectList(wrapper);
        
        int maxSequence = 0;
        for (User user : repairmen) {
            String username = user.getUsername();
            // 检查是否为4位纯数字格式
            if (username != null && username.matches("\\d{4}")) {
                try {
                    int sequence = Integer.parseInt(username);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的工号
                }
            }
        }
        
        // 生成下一个工号
        int nextSequence = maxSequence + 1;
        return String.format("%04d", nextSequence);
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
        
        if (!PasswordUtils.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
    
    @Override
    public List<User> findByRole(String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", role);
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(PasswordUtils.encode(newPassword));
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
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        user.setPassword(PasswordUtils.encode(newPassword));
        updateById(user);
    }
}
