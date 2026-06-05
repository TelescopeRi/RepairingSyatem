
package com.dorm.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    
    User findByUsername(String username);
    
    User register(RegisterDTO dto);
    
    User login(String username, String password);
    
    List<User> findByRole(String role);
    
    void resetPassword(Long userId, String newPassword);
    
    void updateStatus(Long userId, Integer status);
}
