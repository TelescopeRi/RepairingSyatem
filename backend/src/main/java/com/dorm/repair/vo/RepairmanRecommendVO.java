package com.dorm.repair.vo;

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
    
    // 兼容旧代码的getter
    public Double getScore() {
        return totalScore;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getRepairmanId() {
        return repairmanId;
    }
    
    public void setRepairmanId(Long repairmanId) {
        this.repairmanId = repairmanId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Double getSkillScore() {
        return skillScore;
    }
    
    public void setSkillScore(Double skillScore) {
        this.skillScore = skillScore;
    }
    
    public Double getLoadScore() {
        return loadScore;
    }
    
    public void setLoadScore(Double loadScore) {
        this.loadScore = loadScore;
    }
    
    public Double getPerformanceScore() {
        return performanceScore;
    }
    
    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }
    
    public Double getResponseScore() {
        return responseScore;
    }
    
    public void setResponseScore(Double responseScore) {
        this.responseScore = responseScore;
    }
    
    public Double getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }
    
    public Integer getCurrentLoad() {
        return currentLoad;
    }
    
    public void setCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
    }
    
    public Double getCompletionRate() {
        return completionRate;
    }
    
    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
    
    public String getRecommendReason() {
        return recommendReason;
    }
    
    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }
    
    public Boolean getIsBest() {
        return isBest;
    }
    
    public void setIsBest(Boolean isBest) {
        this.isBest = isBest;
    }
    
    public String getRepairmanName() {
        return realName;
    }
}