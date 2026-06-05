package com.dorm.repair.controller;

import com.dorm.repair.dto.RegisterDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试登录-成功")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"2021001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("2021001"))
                .andExpect(jsonPath("$.user.realName").value("张三"));
    }

    @Test
    @DisplayName("测试登录-用户不存在")
    void testLoginUserNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexistent\",\"password\":\"123456\"}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试登录-密码错误")
    void testLoginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"2021001\",\"password\":\"wrong\"}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试学生注册-成功")
    void testRegisterStudent() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("2021003");
        dto.setPassword("123456");
        dto.setRealName("赵六");
        dto.setPhone("13800138005");
        dto.setBuilding("3号楼");
        dto.setDormNumber("301");
        dto.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("2021003"))
                .andExpect(jsonPath("$.realName").value("赵六"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("测试修理工注册-成功")
    void testRegisterRepairman() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("");
        dto.setPassword("123456");
        dto.setRealName("陈师傅");
        dto.setPhone("13800138006");
        dto.setBuilding("3号楼");
        dto.setRole("REPAIR");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.username").value(org.hamcrest.Matchers.startsWith("WX")))
                .andExpect(jsonPath("$.realName").value("陈师傅"))
                .andExpect(jsonPath("$.role").value("REPAIR"));
    }

    @Test
    @DisplayName("测试注册-用户名已存在")
    void testRegisterUsernameExists() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("2021001"); // 已存在
        dto.setPassword("123456");
        dto.setRealName("测试");
        dto.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }
}