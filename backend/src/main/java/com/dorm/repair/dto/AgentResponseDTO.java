package com.dorm.repair.dto;

import java.util.List;

public class AgentResponseDTO {
    
    private String content;
    private String type;
    private List<ParseResultItem> parseResults;
    private boolean success;
    private String error;
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<ParseResultItem> getParseResults() {
        return parseResults;
    }
    
    public void setParseResults(List<ParseResultItem> parseResults) {
        this.parseResults = parseResults;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public static class ParseResultItem {
        private Integer rowIndex;
        private String name;
        private String username;
        private String phone;
        private String role;
        private String building;
        private String dormNumber;
        private String status;
        private String errorMessage;
        
        public Integer getRowIndex() {
            return rowIndex;
        }
        
        public void setRowIndex(Integer rowIndex) {
            this.rowIndex = rowIndex;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getBuilding() {
            return building;
        }
        
        public void setBuilding(String building) {
            this.building = building;
        }
        
        public String getDormNumber() {
            return dormNumber;
        }
        
        public void setDormNumber(String dormNumber) {
            this.dormNumber = dormNumber;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}