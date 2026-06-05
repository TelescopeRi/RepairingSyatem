package com.dorm.repair.controller;

import com.dorm.repair.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("管理员控制器测试")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试获取学生列表")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetStudents() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }

    @Test
    @DisplayName("测试获取修理工列表")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetRepairmen() throws Exception {
        mockMvc.perform(get("/api/admin/repairmen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].role").value("REPAIR"));
    }

    @Test
    @DisplayName("测试添加学生")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateStudent() throws Exception {
        User user = new User();
        user.setUsername("2021004");
        user.setRealName("孙七");
        user.setPhone("13800138007");
        user.setBuilding("1号楼");
        user.setDormNumber("104");

        mockMvc.perform(post("/api/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("2021004"))
                .andExpect(jsonPath("$.realName").value("孙七"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("测试添加修理工")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateRepairman() throws Exception {
        User user = new User();
        user.setUsername("WX202401020001");
        user.setRealName("周师傅");
        user.setPhone("13800138008");
        user.setBuilding("2号楼");

        mockMvc.perform(post("/api/admin/repairmen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("WX202401020001"))
                .andExpect(jsonPath("$.realName").value("周师傅"))
                .andExpect(jsonPath("$.role").value("REPAIR"));
    }

    @Test
    @DisplayName("测试更新用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateUser() throws Exception {
        mockMvc.perform(put("/api/admin/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张三更新\",\"phone\":\"13900139000\",\"building\":\"2号楼\",\"dormNumber\":\"201\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realName").value("张三更新"))
                .andExpect(jsonPath("$.phone").value("13900139000"));
    }

    @Test
    @DisplayName("测试删除用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteUser() throws Exception {
        // 先创建一个用户
        User user = new User();
        user.setUsername("testdelete");
        user.setRealName("测试删除");
        user.setRole("STUDENT");
        user.setStatus(1);

        mockMvc.perform(post("/api/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // 获取ID并删除
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 删除操作
        mockMvc.perform(delete("/api/admin/users/4")) // 假设新创建的用户ID是4
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取楼栋列表")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetBuildings() throws Exception {
        mockMvc.perform(get("/api/admin/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("测试获取故障类型列表")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetFaultTypes() throws Exception {
        mockMvc.perform(get("/api/admin/fault-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("测试获取统计数据")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").exists())
                .andExpect(jsonPath("$.pendingOrders").exists())
                .andExpect(jsonPath("$.completedOrders").exists());
    }
}