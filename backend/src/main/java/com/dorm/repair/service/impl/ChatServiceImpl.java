package com.dorm.repair.service.impl;

import com.dorm.repair.entity.ChatMessage;
import com.dorm.repair.mapper.ChatMessageMapper;
import com.dorm.repair.service.ChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage saveMessage(String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        // 查询最近的limit条消息，按时间倒序
        List<ChatMessage> messages = chatMessageMapper.findRecentBySessionId(sessionId, limit);
        // 反转为正序（ oldest -> newest）
        if (messages != null && !messages.isEmpty()) {
            java.util.Collections.reverse(messages);
        }
        return messages;
    }

    @Override
    public List<ChatMessage> getMessagesByOffset(String sessionId, int limit, int offset) {
        // 查询从offset开始的limit条消息，按时间倒序
        List<ChatMessage> messages = chatMessageMapper.findByOffset(sessionId, limit, offset);
        // 反转为正序
        if (messages != null && !messages.isEmpty()) {
            java.util.Collections.reverse(messages);
        }
        return messages;
    }

    @Override
    public List<ChatMessage> getAllMessages(String sessionId) {
        return chatMessageMapper.findAllBySessionId(sessionId);
    }

    @Override
    public int getMessageCount(String sessionId) {
        return chatMessageMapper.countBySessionId(sessionId);
    }
}
