package com.dorm.repair.vo;

public class TimePredictVO {
    
    private Long orderId;
    private Integer predictedMinutes;
    private String predictedTimeText;
    private String confidenceLevel;
    private Double confidenceScore;
    private String explanation;
    private Integer similarOrderCount;
    private Long repairmanId;
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Integer getPredictedMinutes() {
        return predictedMinutes;
    }
    
    public void setPredictedMinutes(Integer predictedMinutes) {
        this.predictedMinutes = predictedMinutes;
    }
    
    public String getPredictedTimeText() {
        return predictedTimeText;
    }
    
    public void setPredictedTimeText(String predictedTimeText) {
        this.predictedTimeText = predictedTimeText;
    }
    
    public String getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public Integer getSimilarOrderCount() {
        return similarOrderCount;
    }
    
    public void setSimilarOrderCount(Integer similarOrderCount) {
        this.similarOrderCount = similarOrderCount;
    }
    
    public Long getRepairmanId() {
        return repairmanId;
    }
    
    public void setRepairmanId(Long repairmanId) {
        this.repairmanId = repairmanId;
    }
}