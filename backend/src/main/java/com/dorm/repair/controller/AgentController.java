package com.dorm.repair.controller;

import com.dorm.repair.dto.AgentRequestDTO;
import com.dorm.repair.dto.AgentResponseDTO;
import com.dorm.repair.entity.ChatMessage;
import com.dorm.repair.service.AgentService;
import com.dorm.repair.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    
    @Resource
    private AgentService agentService;
    
    @Resource
    private ChatService chatService;
    
    // 使用默认会话ID
    private static final String DEFAULT_SESSION_ID = "default_admin";
    
    @PostMapping("/chat")
    public AgentResponseDTO chat(@RequestBody AgentRequestDTO request) {
        // 保存用户消息到数据库
        chatService.saveMessage(DEFAULT_SESSION_ID, "user", request.getMessage());
        
        // 调用AI服务获取回复
        AgentResponseDTO response = agentService.chat(request.getMessage());
        
        // 保存AI回复到数据库
        chatService.saveMessage(DEFAULT_SESSION_ID, "agent", response.getContent());
        
        return response;
    }
    
    /**
     * 分页获取消息
     * @param offset 偏移量（从0开始）
     * @param limit 每页数量
     * @return 消息列表和总数
     */
    @GetMapping("/messages")
    public Map<String, Object> getMessages(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "3") int limit) {
        List<ChatMessage> messages = chatService.getMessagesByOffset(DEFAULT_SESSION_ID, limit, offset);
        int total = chatService.getMessageCount(DEFAULT_SESSION_ID);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", total);
        result.put("offset", offset);
        result.put("limit", limit);
        return result;
    }
    
    /**
     * 获取最近的消息（按时间倒序）
     * @param limit 消息数量
     * @return 最近的消息列表
     */
    @GetMapping("/messages/recent")
    public Map<String, Object> getRecentMessages(
            @RequestParam(defaultValue = "3") int limit) {
        List<ChatMessage> messages = chatService.getRecentMessages(DEFAULT_SESSION_ID, limit);
        int total = chatService.getMessageCount(DEFAULT_SESSION_ID);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", total);
        return result;
    }
    
    @GetMapping("/messages/all")
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages(DEFAULT_SESSION_ID);
    }
    
    @PostMapping("/parse-file")
    public AgentResponseDTO parseFile(@RequestParam("file") MultipartFile file) {
        return agentService.parseFile(file);
    }
    
    @PostMapping("/batch-import")
    public AgentResponseDTO batchImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        return agentService.batchImport(file, type);
    }
    
    @GetMapping("/summary/repair-status")
    public AgentResponseDTO getRepairStatusSummary() {
        return agentService.summarizeRepairStatus();
    }
    
    @GetMapping("/summary/worker-performance")
    public AgentResponseDTO getWorkerPerformanceSummary() {
        return agentService.summarizeWorkerPerformance();
    }
    
    @GetMapping("/summary/fault-trend")
    public AgentResponseDTO getFaultTrend() {
        return agentService.analyzeFaultTrend();
    }
}
