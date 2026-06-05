package com.dorm.repair.service;

import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.vo.StatisticsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("统计服务测试")
class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RepairOrderService repairOrderService;

    @Test
    @DisplayName("测试获取统计数据")
    void testGetStatistics() {
        StatisticsVO statistics = statisticsService.getStatistics();
        assertNotNull(statistics);
        
        // 统计数据应该不为空
        assertNotNull(statistics.getTotalOrders());
        assertNotNull(statistics.getPendingOrders());
        assertNotNull(statistics.getCompletedOrders());
        assertNotNull(statistics.getPendingAssignOrders());
        assertNotNull(statistics.getAverageCompletionTime());
        assertNotNull(statistics.getRepairmanPerformance());
    }

    @Test
    @DisplayName("测试统计数据-包含新工单")
    void testGetStatisticsWithNewOrders() {
        // 创建一些工单来验证统计
        for (int i = 0; i < 3; i++) {
            RepairOrderDTO dto = new RepairOrderDTO();
            dto.setFaultTypeId(1L);
            dto.setDescription("测试工单" + i);
            dto.setBuilding("1号楼");
            dto.setDormNumber("10" + i);
            repairOrderService.createOrder(2L, dto);
        }

        StatisticsVO statistics = statisticsService.getStatistics();
        
        // 验证统计数据包含新增的工单
        assertTrue(statistics.getTotalOrders() >= 3);
        assertTrue(statistics.getPendingOrders() >= 3);
    }

    @Test
    @DisplayName("测试统计数据-包含已完成工单")
    void testGetStatisticsWithCompletedOrders() {
        // 创建并完成一个工单
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());
        repairOrderService.completeRepair(order.getId(), "已修复");
        repairOrderService.confirmOrder(order.getId());

        StatisticsVO statistics = statisticsService.getStatistics();
        
        // 验证已完成工单统计
        assertTrue(statistics.getCompletedOrders() >= 1);
    }
}