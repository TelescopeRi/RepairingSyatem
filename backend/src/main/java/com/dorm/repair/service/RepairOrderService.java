
package com.dorm.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {
    
    RepairOrder createOrder(Long studentId, RepairOrderDTO dto);
    
    List<RepairOrder> findByStudentId(Long studentId);
    
    List<RepairOrder> findByRepairmanId(Long repairmanId);
    
    List<RepairOrder> findPendingAssign();
    
    List<RepairOrder> findPendingTreat(Long repairmanId);
    
    void assignOrder(Long orderId, Long repairmanId);
    
    void startRepair(Long orderId);
    
    void completeRepair(Long orderId, String remark, java.util.List<String> images);

    void cancelOrder(Long orderId, String reason);

    List<RepairOrder> findByStatus(String status);
}
