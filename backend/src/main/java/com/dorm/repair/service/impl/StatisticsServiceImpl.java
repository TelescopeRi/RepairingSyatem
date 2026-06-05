package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.EvaluationMapper;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.mapper.RepairOrderMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.StatisticsService;
import com.dorm.repair.vo.RepairmanPerformanceVO;
import com.dorm.repair.vo.StatisticsVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final int OVERTIME_HOURS = 48; // 超时阈值：48小时
    
    @Resource
    private RepairOrderMapper repairOrderMapper;
    
    @Resource
    private EvaluationMapper evaluationMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 今日新增报修
        LocalDateTime todayStart = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        QueryWrapper<RepairOrder> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("submit_time", todayStart);
        long todayOrders = repairOrderMapper.selectCount(todayWrapper);
        vo.setTodayOrders(todayOrders);
        
        // 2. 待分配工单 (PENDING_ASSIGN)
        QueryWrapper<RepairOrder> pendingAssignWrapper = new QueryWrapper<>();
        pendingAssignWrapper.eq("status", "PENDING_ASSIGN");
        long pendingAssignOrders = repairOrderMapper.selectCount(pendingAssignWrapper);
        vo.setPendingAssignOrders(pendingAssignOrders);
        
        // 3. 处理中工单 (ASSIGNED, PROCESSING)
        QueryWrapper<RepairOrder> processingWrapper = new QueryWrapper<>();
        processingWrapper.in("status", Arrays.asList("ASSIGNED", "PROCESSING"));
        long processingOrders = repairOrderMapper.selectCount(processingWrapper);
        vo.setProcessingOrders(processingOrders);
        
        // 4. 超时工单数 (超过48小时未分配的PENDING_ASSIGN工单)
        LocalDateTime overtimeThreshold = now.minusHours(OVERTIME_HOURS);
        QueryWrapper<RepairOrder> overtimeWrapper = new QueryWrapper<>();
        overtimeWrapper.eq("status", "PENDING_ASSIGN");
        overtimeWrapper.lt("submit_time", overtimeThreshold);
        long overtimeOrders = repairOrderMapper.selectCount(overtimeWrapper);
        vo.setOvertimeOrders(overtimeOrders);
        
        // 5. 本月完成率
        LocalDateTime monthStart = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0);
        QueryWrapper<RepairOrder> monthTotalWrapper = new QueryWrapper<>();
        monthTotalWrapper.ge("submit_time", monthStart);
        long monthTotal = repairOrderMapper.selectCount(monthTotalWrapper);
        
        QueryWrapper<RepairOrder> monthCompletedWrapper = new QueryWrapper<>();
        monthCompletedWrapper.ge("submit_time", monthStart);
        monthCompletedWrapper.eq("status", "COMPLETED");
        long monthCompleted = repairOrderMapper.selectCount(monthCompletedWrapper);
        
        double monthCompletionRate = monthTotal > 0 ? (double) monthCompleted / monthTotal * 100 : 0;
        vo.setMonthCompletionRate(Math.round(monthCompletionRate * 100.0) / 100.0);
        
        // 6. 平均处理时长
        QueryWrapper<RepairOrder> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("status", "COMPLETED");
        List<RepairOrder> completedList = repairOrderMapper.selectList(completedWrapper);
        
        double avgHours = completedList.stream()
                .filter(o -> o.getSubmitTime() != null && o.getCompleteTime() != null)
                .mapToDouble(o -> Duration.between(o.getSubmitTime(), o.getCompleteTime()).toHours())
                .average()
                .orElse(0);
        vo.setAvgProcessingHours(Math.round(avgHours * 100.0) / 100.0);
        
        // 7. 总订单数和总完成数
        QueryWrapper<RepairOrder> allWrapper = new QueryWrapper<>();
        long totalOrders = repairOrderMapper.selectCount(allWrapper);
        vo.setTotalOrders(totalOrders);
        vo.setCompletedOrders((long) completedList.size());
        double completionRate = totalOrders > 0 ? (double) completedList.size() / totalOrders * 100 : 0;
        vo.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        
        // 8. 近7天趋势
        List<StatisticsVO.DailyOrderCount> dailyTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.of(now.minusDays(i).toLocalDate(), LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(now.minusDays(i).toLocalDate(), LocalTime.MAX);
            QueryWrapper<RepairOrder> dayWrapper = new QueryWrapper<>();
            dayWrapper.ge("submit_time", dayStart);
            dayWrapper.le("submit_time", dayEnd);
            long dayCount = repairOrderMapper.selectCount(dayWrapper);
            String dateStr = now.minusDays(i).format(formatter);
            dailyTrend.add(new StatisticsVO.DailyOrderCount(dateStr, dayCount));
        }
        vo.setDailyTrend(dailyTrend);
        
        // 9. 各楼栋报修数量
        QueryWrapper<RepairOrder> allOrdersWrapper = new QueryWrapper<>();
        List<RepairOrder> allOrders = repairOrderMapper.selectList(allOrdersWrapper);
        Map<String, Long> buildingCount = allOrders.stream()
                .filter(o -> o.getBuilding() != null && !o.getBuilding().isEmpty())
                .collect(Collectors.groupingBy(RepairOrder::getBuilding, Collectors.counting()));
        vo.setBuildingCount(buildingCount);
        
        // 10. 故障类型占比（按所有工单统计）
        Map<String, Long> faultTypeCount = new HashMap<>();
        List<FaultType> faultTypes = faultTypeMapper.selectAllEnabled();
        Map<Long, String> typeMap = faultTypes.stream()
                .collect(Collectors.toMap(FaultType::getId, FaultType::getName));
        
        Map<Long, Long> typeCountMap = allOrders.stream()
                .filter(o -> o.getFaultTypeId() != null)
                .collect(Collectors.groupingBy(RepairOrder::getFaultTypeId, Collectors.counting()));
        
        for (FaultType type : faultTypes) {
            faultTypeCount.put(type.getName(), typeCountMap.getOrDefault(type.getId(), 0L));
        }
        vo.setFaultTypeCount(faultTypeCount);
        
        // 11. 维修工绩效排名
        List<User> repairmen = userMapper.selectByRole("REPAIR");
        List<RepairmanPerformanceVO> performanceList = new ArrayList<>();
        
        for (User repairman : repairmen) {
            QueryWrapper<RepairOrder> repairmanWrapper = new QueryWrapper<>();
            repairmanWrapper.eq("repairman_id", repairman.getId());
            repairmanWrapper.eq("status", "COMPLETED");
            long count = repairOrderMapper.selectCount(repairmanWrapper);
            
            List<Evaluation> evaluations = evaluationMapper.selectByRepairmanId(repairman.getId());
            double avgRating = evaluations.stream()
                    .mapToInt(Evaluation::getRating)
                    .average()
                    .orElse(0);
            
            List<RepairOrder> repairmanOrders = repairOrderMapper.selectList(repairmanWrapper);
            double repairmanAvgHours = repairmanOrders.stream()
                    .filter(o -> o.getSubmitTime() != null && o.getCompleteTime() != null)
                    .mapToDouble(o -> Duration.between(o.getSubmitTime(), o.getCompleteTime()).toHours())
                    .average()
                    .orElse(0);
            
            RepairmanPerformanceVO performance = new RepairmanPerformanceVO();
            performance.setRepairmanId(repairman.getId());
            performance.setRepairmanName(repairman.getRealName());
            performance.setCompletedCount(count);
            performance.setAvgRating(Math.round(avgRating * 100.0) / 100.0);
            performance.setAvgProcessingHours(Math.round(repairmanAvgHours * 100.0) / 100.0);
            performanceList.add(performance);
        }
        
        performanceList.sort((a, b) -> Long.compare(b.getCompletedCount(), a.getCompletedCount()));
        vo.setRepairmanPerformance(performanceList);
        
        return vo;
    }
}
