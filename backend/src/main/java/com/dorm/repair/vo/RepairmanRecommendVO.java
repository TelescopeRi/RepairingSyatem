package com.dorm.repair.vo;

import lombok.Data;

@Data
public class RepairmanRecommendVO {
    
    private Long orderId;
    
    private Long repairmanId;
    private String username;
    private String realName;
    private String phone;
    
    private Double skillScore;
    private Double loadScore;
    private Double performanceScore;
    private Double responseScore;
    private Double totalScore;
    
    private Integer currentLoad;
    private Double completionRate;
    private String recommendReason;
    
    private Boolean isBest;
}