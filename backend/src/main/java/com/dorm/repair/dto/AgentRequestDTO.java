package com.dorm.repair.dto;

public class AgentRequestDTO {
    
    private String message;
    private String type; // chat, summary, import
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}