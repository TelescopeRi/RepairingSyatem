package com.dorm.repair.service;

import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.User;
import com.dorm.repair.service.impl.UserServiceImpl;
import com.dorm.repair.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户服务测试")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUtils passwordUtils;

    @Test
    @DisplayName("测试根据用户名查找用户")
    void testFindByUsername() {
        User user = userService.findByUsername("2021001");
        assertNotNull(user);
        assertEquals("张三", user.getRealName());
        assertEquals("STUDENT", user.getRole());
    }

    @Test
    @DisplayName("测试根据用户名查找不存在的用户")
    void testFindByUsernameNotFound() {
        User user = userService.findByUsername("nonexistent");
        assertNull(user);
    }

    @Test
    @DisplayName("测试学生注册")
    void testRegisterStudent() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("2021002");
        dto.setPassword("123456");
        dto.setRealName("李四");
        dto.setPhone("13800138003");
        dto.setBuilding("2号楼");
        dto.setDormNumber("202");
        dto.setRole("STUDENT");

        User user = userService.register(dto);

        assertNotNull(user);
        assertEquals("2021002", user.getUsername());
        assertEquals("李四", user.getRealName());
        assertEquals("STUDENT", user.getRole());
        assertTrue(passwordUtils.matches("123456", user.getPassword()));
    }

    @Test
    @DisplayName("测试学生注册-用户名已存在")
    void testRegisterStudentUsernameExists() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("2021001"); // 已存在的学号
        dto.setPassword("123456");
        dto.setRealName("王五");
        dto.setRole("STUDENT");

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    @DisplayName("测试修理工注册-自动生成工号")
    void testRegisterRepairman() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("");
        dto.setPassword("123456");
        dto.setRealName("王师傅");
        dto.setPhone("13800138004");
        dto.setBuilding("2号楼");
        dto.setRole("REPAIR");

        User user = userService.register(dto);

        assertNotNull(user);
        assertTrue(user.getUsername().startsWith("WX"));
        assertEquals(14, user.getUsername().length()); // WX + 8位日期 + 4位序号
        assertEquals("王师傅", user.getRealName());
        assertEquals("REPAIR", user.getRole());
    }

    @Test
    @DisplayName("测试登录-成功")
    void testLoginSuccess() {
        User user = userService.login("2021001", "123456");
        assertNotNull(user);
        assertEquals("张三", user.getRealName());
    }

    @Test
    @DisplayName("测试登录-用户不存在")
    void testLoginUserNotFound() {
        assertThrows(RuntimeException.class, () -> userService.login("nonexistent", "123456"));
    }

    @Test
    @DisplayName("测试登录-密码错误")
    void testLoginWrongPassword() {
        assertThrows(RuntimeException.class, () -> userService.login("2021001", "wrongpassword"));
    }

    @Test
    @DisplayName("测试根据角色查找用户")
    void testFindByRole() {
        List<User> students = userService.findByRole("STUDENT");
        assertFalse(students.isEmpty());
        assertTrue(students.stream().allMatch(u -> "STUDENT".equals(u.getRole())));

        List<User> repairmen = userService.findByRole("REPAIR");
        assertFalse(repairmen.isEmpty());
        assertTrue(repairmen.stream().allMatch(u -> "REPAIR".equals(u.getRole())));
    }

    @Test
    @DisplayName("测试重置密码")
    void testResetPassword() {
        userService.resetPassword(1L, "newpassword");
        User user = userService.getById(1L);
        assertTrue(passwordUtils.matches("newpassword", user.getPassword()));
    }

    @Test
    @DisplayName("测试更新用户状态")
    void testUpdateStatus() {
        userService.updateStatus(2L, 0); // 禁用学生
        User user = userService.getById(2L);
        assertEquals(0, user.getStatus());

        userService.updateStatus(2L, 1); // 启用学生
        user = userService.getById(2L);
        assertEquals(1, user.getStatus());
    }
}