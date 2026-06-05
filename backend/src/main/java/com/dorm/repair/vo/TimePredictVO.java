package com.dorm.repair.vo;

import lombok.Data;

@Data
public class TimePredictVO {
    
    private Long orderId;
    private Integer predictedMinutes;
    private String predictedTimeText;
    private String confidenceLevel;
    private Double confidenceScore;
    private String explanation;
    private Integer similarOrderCount;
}