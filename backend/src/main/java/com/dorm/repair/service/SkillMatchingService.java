package com.dorm.repair.service;

import com.dorm.repair.vo.RepairmanRecommendVO;

import java.util.List;

public interface SkillMatchingService {
    
    /**
     * 获取工单的维修工推荐列表
     * @param orderId 工单ID
     * @return 推荐列表（按综合得分降序）
     */
    List<RepairmanRecommendVO> getRecommendations(Long orderId);
    
    /**
     * 获取工单的最佳推荐维修工
     * @param orderId 工单ID
     * @return 最佳推荐的维修工
     */
    RepairmanRecommendVO getBestRecommendation(Long orderId);
    
    /**
     * 批量获取工单推荐
     * @param orderIds 工单ID列表
     * @return 每个工单的最佳推荐
     */
    List<RepairmanRecommendVO> batchRecommend(List<Long> orderIds);
}