package com.dorm.repair.vo;

import java.util.List;
import java.util.Map;

public class StatisticsVO {

    // 卡片指标
    private Long todayOrders;          // 今日新增报修
    private Long pendingAssignOrders; // 待分配工单
    private Long processingOrders;    // 处理中工单
    private Double monthCompletionRate; // 本月完成率
    private Double avgProcessingHours; // 平均处理时长
    private Long overtimeOrders;      // 超时工单数

    // 原有字段
    private Long totalOrders;
    private Long completedOrders;
    private Double completionRate;

    // 图表数据
    private Map<String, Long> faultTypeCount;  // 故障类型占比
    private Map<String, Long> buildingCount;  // 各楼栋报修数量
    private List<DailyOrderCount> dailyTrend;  // 近7天趋势

    private List<RepairmanPerformanceVO> repairmanPerformance;

    // Getter and Setter
    public Long getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public Long getPendingAssignOrders() {
        return pendingAssignOrders;
    }

    public void setPendingAssignOrders(Long pendingAssignOrders) {
        this.pendingAssignOrders = pendingAssignOrders;
    }

    public Long getProcessingOrders() {
        return processingOrders;
    }

    public void setProcessingOrders(Long processingOrders) {
        this.processingOrders = processingOrders;
    }

    public Double getMonthCompletionRate() {
        return monthCompletionRate;
    }

    public void setMonthCompletionRate(Double monthCompletionRate) {
        this.monthCompletionRate = monthCompletionRate;
    }

    public Double getAvgProcessingHours() {
        return avgProcessingHours;
    }

    public void setAvgProcessingHours(Double avgProcessingHours) {
        this.avgProcessingHours = avgProcessingHours;
    }

    public Long getOvertimeOrders() {
        return overtimeOrders;
    }

    public void setOvertimeOrders(Long overtimeOrders) {
        this.overtimeOrders = overtimeOrders;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Map<String, Long> getFaultTypeCount() {
        return faultTypeCount;
    }

    public void setFaultTypeCount(Map<String, Long> faultTypeCount) {
        this.faultTypeCount = faultTypeCount;
    }

    public Map<String, Long> getBuildingCount() {
        return buildingCount;
    }

    public void setBuildingCount(Map<String, Long> buildingCount) {
        this.buildingCount = buildingCount;
    }

    public List<DailyOrderCount> getDailyTrend() {
        return dailyTrend;
    }

    public void setDailyTrend(List<DailyOrderCount> dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public List<RepairmanPerformanceVO> getRepairmanPerformance() {
        return repairmanPerformance;
    }

    public void setRepairmanPerformance(List<RepairmanPerformanceVO> repairmanPerformance) {
        this.repairmanPerformance = repairmanPerformance;
    }

    // 内部类：每日工单数量
    public static class DailyOrderCount {
        private String date;
        private Long count;

        public DailyOrderCount() {}

        public DailyOrderCount(String date, Long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}
