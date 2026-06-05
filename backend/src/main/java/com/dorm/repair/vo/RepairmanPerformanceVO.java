package com.dorm.repair.vo;

public class RepairmanPerformanceVO {

    private Long repairmanId;

    private String repairmanName;

    private Long completedCount;

    private Double avgRating;

    private Double avgProcessingHours;

    public Long getRepairmanId() {
        return repairmanId;
    }

    public void setRepairmanId(Long repairmanId) {
        this.repairmanId = repairmanId;
    }

    public String getRepairmanName() {
        return repairmanName;
    }

    public void setRepairmanName(String repairmanName) {
        this.repairmanName = repairmanName;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Double getAvgProcessingHours() {
        return avgProcessingHours;
    }

    public void setAvgProcessingHours(Double avgProcessingHours) {
        this.avgProcessingHours = avgProcessingHours;
    }
}
