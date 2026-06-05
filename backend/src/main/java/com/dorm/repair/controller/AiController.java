package com.dorm.repair.controller;

import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.SkillMatchingService;
import com.dorm.repair.service.TimePredictService;
import com.dorm.repair.vo.RepairmanRecommendVO;
import com.dorm.repair.vo.TimePredictVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Resource
    private SkillMatchingService skillMatchingService;
    
    @Resource
    private TimePredictService timePredictService;
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @GetMapping("/recommend/{orderId}")
    public List<RepairmanRecommendVO> getRecommendations(@PathVariable Long orderId) {
        return skillMatchingService.getRecommendations(orderId);
    }
    
    @GetMapping("/recommend/best/{orderId}")
    public RepairmanRecommendVO getBestRecommendation(@PathVariable Long orderId) {
        return skillMatchingService.getBestRecommendation(orderId);
    }
    
    @PostMapping("/recommend/batch")
    public List<RepairmanRecommendVO> batchRecommend(@RequestBody List<Long> orderIds) {
        return skillMatchingService.batchRecommend(orderIds);
    }
    
    @GetMapping("/predict/time/{orderId}")
    public TimePredictVO predictTime(@PathVariable Long orderId) {
        return timePredictService.predictTime(orderId);
    }
    
    @GetMapping("/predict/time/{orderId}/repairman/{repairmanId}")
    public TimePredictVO predictTimeWithRepairman(@PathVariable Long orderId, @PathVariable Long repairmanId) {
        return timePredictService.predictTime(orderId, repairmanId);
    }
    
    @PostMapping("/assign/auto/{orderId}")
    public RepairOrder autoAssign(@PathVariable Long orderId) {
        RepairmanRecommendVO best = skillMatchingService.getBestRecommendation(orderId);
        if (best == null) {
            throw new RuntimeException("没有可用的维修工");
        }
        
        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        
        if (!"PENDING_ASSIGN".equals(order.getStatus())) {
            throw new RuntimeException("工单状态不允许分配");
        }
        
        repairOrderService.assignOrder(orderId, best.getRepairmanId());
        return repairOrderService.getById(orderId);
    }
    
    @PostMapping("/assign/batch")
    public List<RepairOrder> batchAutoAssign(@RequestBody List<Long> orderIds) {
        return orderIds.stream()
                .map(orderId -> {
                    try {
                        RepairmanRecommendVO best = skillMatchingService.getBestRecommendation(orderId);
                        if (best == null) return null;
                        
                        RepairOrder order = repairOrderService.getById(orderId);
                        if (order == null || !"PENDING_ASSIGN".equals(order.getStatus())) {
                            return null;
                        }
                        
                        repairOrderService.assignOrder(orderId, best.getRepairmanId());
                        return repairOrderService.getById(orderId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
    }
}