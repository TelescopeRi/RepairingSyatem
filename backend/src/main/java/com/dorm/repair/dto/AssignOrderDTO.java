package com.dorm.repair.dto;

public class AssignOrderDTO {

    private Long orderId;

    private Long repairmanId;

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
}
