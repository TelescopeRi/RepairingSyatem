package com.dorm.repair.service;

import com.dorm.repair.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatMessage saveMessage(String sessionId, String role, String content);
    List<ChatMessage> getRecentMessages(String sessionId, int limit);
    List<ChatMessage> getMessagesByOffset(String sessionId, int limit, int offset);
    List<ChatMessage> getAllMessages(String sessionId);
    int getMessageCount(String sessionId);
}
