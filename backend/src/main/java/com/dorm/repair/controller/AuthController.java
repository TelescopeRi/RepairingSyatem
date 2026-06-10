package com.dorm.repair.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.repair.dto.LoginDTO;
import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.User;
import com.dorm.repair.service.UserService;
import com.dorm.repair.utils.JwtUtils;
import com.dorm.repair.utils.PasswordUtils;
import com.dorm.repair.vo.LoginVO;
import com.dorm.repair.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public LoginVO login(@RequestBody LoginDTO dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(convertToVO(user));
        
        return vo;
    }
    
    @PostMapping("/register")
    public UserVO register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto);
        return convertToVO(user);
    }
    
    /**
     * 创建管理员账号（首次使用时调用，无需认证）
     * 创建一个默认的管理员账号
     */
    @PostMapping("/init-admin")
    public UserVO initAdmin(@RequestParam(defaultValue = "admin") String username, 
                           @RequestParam(defaultValue = "admin123") String password,
                           @RequestParam(defaultValue = "管理员") String realName) {
        // 检查是否已存在管理员（包括已删除的）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "ADMIN");
        List<User> admins = userService.list(wrapper);
        if (!admins.isEmpty()) {
            throw new RuntimeException("管理员账号已存在");
        }
        
        // 创建管理员
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(PasswordUtils.encode(password));
        admin.setRealName(realName);
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        
        userService.save(admin);
        return convertToVO(admin);
    }
    
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setBuilding(user.getBuilding());
        vo.setDormNumber(user.getDormNumber());
        return vo;
    }
}
