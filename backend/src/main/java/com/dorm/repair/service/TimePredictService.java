package com.dorm.repair.service;

import com.dorm.repair.vo.TimePredictVO;

public interface TimePredictService {
    
    /**
     * 预测工单处理时长
     * @param orderId 工单ID
     * @return 预测结果
     */
    TimePredictVO predictTime(Long orderId);
    
    /**
     * 预测工单处理时长（指定维修工）
     * @param orderId 工单ID
     * @param repairmanId 维修工ID
     * @return 预测结果
     */
    TimePredictVO predictTime(Long orderId, Long repairmanId);
}