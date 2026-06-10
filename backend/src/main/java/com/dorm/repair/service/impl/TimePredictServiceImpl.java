package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.TimePredictService;
import com.dorm.repair.vo.TimePredictVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimePredictServiceImpl implements TimePredictService {
    
    @Resource
    private RepairOrderService repairOrderService;
    
    private static final Map<String, Integer> BASE_TIME_MAP = new HashMap<>();
    
    static {
        BASE_TIME_MAP.put("水电维修", 120);
        BASE_TIME_MAP.put("家具维修", 90);
        BASE_TIME_MAP.put("电器维修", 150);
        BASE_TIME_MAP.put("网络维修", 60);
        BASE_TIME_MAP.put("门锁维修", 45);
        BASE_TIME_MAP.put("墙面维修", 180);
        BASE_TIME_MAP.put("其他", 120);
    }
    
    @Override
    public TimePredictVO predictTime(Long orderId) {
        return predictTime(orderId, null);
    }
    
    @Override
    public TimePredictVO predictTime(Long orderId, Long repairmanId) {
        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        
        TimePredictVO vo = new TimePredictVO();
        vo.setOrderId(orderId);
        vo.setRepairmanId(repairmanId);
        
        int baseMinutes = getBaseTime(order.getFaultTypeId());
        int similarCount = countSimilarOrders(order);
        
        double repairmanFactor = 1.0;
        if (repairmanId != null) {
            repairmanFactor = getRepairmanFactor(repairmanId, order.getFaultTypeId());
        }
        
        int predictedMinutes = (int) (baseMinutes * repairmanFactor);
        
        vo.setPredictedMinutes(predictedMinutes);
        vo.setPredictedTimeText(formatTime(predictedMinutes));
        vo.setSimilarOrderCount(similarCount);
        
        double confidence = calculateConfidence(similarCount, predictedMinutes);
        vo.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);
        vo.setConfidenceLevel(getConfidenceLevel(confidence));
        vo.setExplanation(generateExplanation(vo, order));
        
        return vo;
    }
    
    private int getBaseTime(Long faultTypeId) {
        String faultTypeName = getFaultTypeName(faultTypeId);
        return BASE_TIME_MAP.getOrDefault(faultTypeName, 120);
    }
    
    private String getFaultTypeName(Long faultTypeId) {
        Map<Long, String> typeMap = new HashMap<>();
        typeMap.put(1L, "水电维修");
        typeMap.put(2L, "家具维修");
        typeMap.put(3L, "电器维修");
        typeMap.put(4L, "网络维修");
        typeMap.put(5L, "门锁维修");
        typeMap.put(6L, "墙面维修");
        typeMap.put(7L, "其他");
        return typeMap.getOrDefault(faultTypeId, "其他");
    }
    
    private int countSimilarOrders(RepairOrder order) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("fault_type_id", order.getFaultTypeId());
        wrapper.eq("status", "COMPLETED");
        wrapper.ge("submit_time", thirtyDaysAgo);
        
        if (order.getBuilding() != null && !order.getBuilding().isEmpty()) {
            wrapper.eq("building", order.getBuilding());
        }
        
        return (int) repairOrderService.count(wrapper);
    }
    
    private double getRepairmanFactor(Long repairmanId, Long faultTypeId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("repairman_id", repairmanId);
        wrapper.eq("fault_type_id", faultTypeId);
        wrapper.eq("status", "COMPLETED");
        wrapper.ge("submit_time", thirtyDaysAgo);
        
        List<RepairOrder> orders = repairOrderService.list(wrapper);
        
        if (orders.isEmpty()) {
            return 1.0;
        }
        
        long totalMinutes = 0;
        for (RepairOrder order : orders) {
            if (order.getCompleteTime() != null && order.getAssignTime() != null) {
                totalMinutes += java.time.Duration.between(order.getAssignTime(), order.getCompleteTime()).toMinutes();
            }
        }
        
        int baseTime = getBaseTime(faultTypeId);
        double avgMinutes = (double) totalMinutes / orders.size();
        
        return avgMinutes / baseTime;
    }
    
    private double calculateConfidence(int similarCount, int predictedMinutes) {
        if (similarCount >= 20) {
            return 0.9;
        } else if (similarCount >= 10) {
            return 0.8;
        } else if (similarCount >= 5) {
            return 0.7;
        } else if (similarCount >= 1) {
            return 0.55;
        } else {
            return 0.4;
        }
    }
    
    private String getConfidenceLevel(double confidence) {
        if (confidence >= 0.75) {
            return "高";
        } else if (confidence >= 0.55) {
            return "中";
        } else {
            return "低";
        }
    }
    
    private String formatTime(int minutes) {
        if (minutes < 60) {
            return "约 " + minutes + "分钟";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return "约 " + hours + "小时";
            } else {
                return "约 " + hours + "小时" + remainingMinutes + "分钟";
            }
        }
    }
    
    private String generateExplanation(TimePredictVO vo, RepairOrder order) {
        String typeName = getFaultTypeName(order.getFaultTypeId());
        StringBuilder sb = new StringBuilder();
        
        sb.append("历史数据显示，");
        sb.append(typeName);
        sb.append("通常在");
        sb.append(vo.getPredictedTimeText());
        sb.append("内完成");
        
        if (vo.getSimilarOrderCount() > 0) {
            sb.append("，过去30天内有").append(vo.getSimilarOrderCount()).append("个相似工单");
        }
        
        return sb.toString();
    }
}