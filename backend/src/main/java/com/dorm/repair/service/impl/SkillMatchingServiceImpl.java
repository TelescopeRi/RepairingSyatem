package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.SkillMatchingService;
import com.dorm.repair.service.UserService;
import com.dorm.repair.vo.RepairmanRecommendVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillMatchingServiceImpl implements SkillMatchingService {
    
    @Resource
    private UserService userService;
    
    @Resource
    private RepairOrderService repairOrderService;
    
    private static final double W1 = 0.4;
    private static final double W2 = 0.25;
    private static final double W3 = 0.2;
    private static final double W4 = 0.15;
    
    @Override
    public List<RepairmanRecommendVO> getRecommendations(Long orderId) {
        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            return Collections.emptyList();
        }
        
        List<User> repairmen = userService.findByRole("REPAIR");
        List<RepairmanRecommendVO> recommendations = new ArrayList<>();
        
        for (User repairman : repairmen) {
            if (repairman.getStatus() == 0) {
                continue;
            }
            
            RepairmanRecommendVO vo = calculateScores(order, repairman);
            vo.setOrderId(orderId);
            recommendations.add(vo);
        }
        
        recommendations.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
        
        if (!recommendations.isEmpty()) {
            recommendations.get(0).setIsBest(true);
            recommendations.get(0).setRecommendReason(generateRecommendReason(recommendations.get(0)));
        }
        
        return recommendations;
    }
    
    @Override
    public RepairmanRecommendVO getBestRecommendation(Long orderId) {
        List<RepairmanRecommendVO> recommendations = getRecommendations(orderId);
        return recommendations.isEmpty() ? null : recommendations.get(0);
    }
    
    @Override
    public List<RepairmanRecommendVO> batchRecommend(List<Long> orderIds) {
        List<RepairmanRecommendVO> results = new ArrayList<>();
        for (Long orderId : orderIds) {
            RepairmanRecommendVO best = getBestRecommendation(orderId);
            if (best != null) {
                results.add(best);
            }
        }
        return results;
    }
    
    private RepairmanRecommendVO calculateScores(RepairOrder order, User repairman) {
        RepairmanRecommendVO vo = new RepairmanRecommendVO();
        vo.setRepairmanId(repairman.getId());
        vo.setUsername(repairman.getUsername());
        vo.setRealName(repairman.getRealName());
        vo.setPhone(repairman.getPhone());
        
        double skillScore = calculateSkillScore(order, repairman);
        double loadScore = calculateLoadScore(repairman);
        double performanceScore = calculatePerformanceScore(repairman);
        double responseScore = calculateResponseScore(repairman);
        
        vo.setSkillScore(Math.round(skillScore * 100.0) / 100.0);
        vo.setLoadScore(Math.round(loadScore * 100.0) / 100.0);
        vo.setPerformanceScore(Math.round(performanceScore * 100.0) / 100.0);
        vo.setResponseScore(Math.round(responseScore * 100.0) / 100.0);
        
        double totalScore = skillScore * W1 + loadScore * W2 + performanceScore * W3 + responseScore * W4;
        vo.setTotalScore(Math.round(totalScore * 100.0) / 100.0);
        
        vo.setCurrentLoad(getCurrentLoad(repairman.getId()));
        vo.setCompletionRate(getCompletionRate(repairman.getId()));
        vo.setIsBest(false);
        
        return vo;
    }
    
    private double calculateSkillScore(RepairOrder order, User repairman) {
        String specialtyIds = repairman.getSpecialtyIds();
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            return 0.5;
        }
        
        String[] specialties = specialtyIds.split(",");
        String faultTypeId = String.valueOf(order.getFaultTypeId());
        
        for (String specialty : specialties) {
            if (specialty.trim().equals(faultTypeId)) {
                return 0.9;
            }
        }
        
        return 0.4;
    }
    
    private double calculateLoadScore(User repairman) {
        int load = getCurrentLoad(repairman.getId());
        
        if (load == 0) {
            return 1.0;
        } else if (load == 1) {
            return 0.85;
        } else if (load == 2) {
            return 0.7;
        } else if (load == 3) {
            return 0.5;
        } else {
            return 0.2;
        }
    }
    
    private double calculatePerformanceScore(User repairman) {
        double completionRate = getCompletionRate(repairman.getId());
        double avgRating = getAverageRating(repairman.getId());
        
        return (completionRate * 0.6 + avgRating * 0.4);
    }
    
    private double calculateResponseScore(User repairman) {
        Double avgResponseMinutes = getAverageResponseTime(repairman.getId());
        
        if (avgResponseMinutes == null) {
            return 0.7;
        }
        
        if (avgResponseMinutes <= 10) {
            return 1.0;
        } else if (avgResponseMinutes <= 30) {
            return 0.8;
        } else if (avgResponseMinutes <= 60) {
            return 0.6;
        } else {
            return 0.4;
        }
    }
    
    private int getCurrentLoad(Long repairmanId) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("repairman_id", repairmanId);
        wrapper.in("status", "ASSIGNED", "PROCESSING");
        return (int) repairOrderService.count(wrapper);
    }
    
    private double getCompletionRate(Long repairmanId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        QueryWrapper<RepairOrder> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("repairman_id", repairmanId);
        totalWrapper.ge("submit_time", thirtyDaysAgo);
        long total = repairOrderService.count(totalWrapper);
        
        if (total == 0) {
            return 0.85;
        }
        
        QueryWrapper<RepairOrder> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("repairman_id", repairmanId);
        completedWrapper.eq("status", "COMPLETED");
        completedWrapper.ge("submit_time", thirtyDaysAgo);
        long completed = repairOrderService.count(completedWrapper);
        
        return (double) completed / total;
    }
    
    private double getAverageRating(Long repairmanId) {
        return 4.5;
    }
    
    private Double getAverageResponseTime(Long repairmanId) {
        return null;
    }
    
    private String generateRecommendReason(RepairmanRecommendVO vo) {
        List<String> reasons = new ArrayList<>();
        
        if (vo.getSkillScore() >= 0.8) {
            reasons.add("技能匹配度高");
        }
        if (vo.getLoadScore() >= 0.8) {
            reasons.add("当前负载较低");
        }
        if (vo.getPerformanceScore() >= 0.85) {
            reasons.add("历史表现优秀");
        }
        if (vo.getResponseScore() >= 0.8) {
            reasons.add("响应速度快");
        }
        
        if (reasons.isEmpty()) {
            return "综合评分最高";
        }
        
        return String.join("且", reasons);
    }
}