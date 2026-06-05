package com.dorm.repair.service;

import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.service.impl.RepairOrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("工单服务测试")
class RepairOrderServiceTest {

    @Autowired
    private RepairOrderService repairOrderService;

    @Test
    @DisplayName("测试创建工单")
    void testCreateOrder() {
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("水龙头漏水");
        dto.setUrgency("NORMAL");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");

        RepairOrder order = repairOrderService.createOrder(2L, dto); // 学生ID为2

        assertNotNull(order);
        assertEquals(2L, order.getStudentId());
        assertEquals("PENDING_ASSIGN", order.getStatus());
        assertEquals("水龙头漏水", order.getDescription());
    }

    @Test
    @DisplayName("测试创建工单-学生不存在")
    void testCreateOrderStudentNotFound() {
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试");

        assertThrows(RuntimeException.class, () -> repairOrderService.createOrder(999L, dto));
    }

    @Test
    @DisplayName("测试分配工单")
    void testAssignOrder() {
        // 先创建一个工单
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);

        // 分配给修理工
        repairOrderService.assignOrder(order.getId(), 3L); // 修理工ID为3

        RepairOrder assignedOrder = repairOrderService.getById(order.getId());
        assertEquals(3L, assignedOrder.getRepairmanId());
        assertEquals("PENDING_TREAT", assignedOrder.getStatus());
        assertNotNull(assignedOrder.getAssignTime());
    }

    @Test
    @DisplayName("测试分配工单-工单不存在")
    void testAssignOrderNotFound() {
        assertThrows(RuntimeException.class, () -> repairOrderService.assignOrder(999L, 3L));
    }

    @Test
    @DisplayName("测试开始维修")
    void testStartRepair() {
        // 创建并分配工单
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);
        repairOrderService.assignOrder(order.getId(), 3L);

        // 开始维修
        repairOrderService.startRepair(order.getId());

        RepairOrder inProgressOrder = repairOrderService.getById(order.getId());
        assertEquals("IN_PROGRESS", inProgressOrder.getStatus());
        assertNotNull(inProgressOrder.getStartTime());
    }

    @Test
    @DisplayName("测试完成维修")
    void testCompleteRepair() {
        // 创建、分配、开始维修
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());

        // 完成维修
        repairOrderService.completeRepair(order.getId(), "已修复");

        RepairOrder completedOrder = repairOrderService.getById(order.getId());
        assertEquals("PENDING_CONFIRM", completedOrder.getStatus());
        assertEquals("已修复", completedOrder.getRemark());
        assertNotNull(completedOrder.getCompleteTime());
    }

    @Test
    @DisplayName("测试确认工单完成")
    void testConfirmOrder() {
        // 创建、分配、开始、完成维修
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());
        repairOrderService.completeRepair(order.getId(), "已修复");

        // 管理员确认完成
        repairOrderService.confirmOrder(order.getId());

        RepairOrder confirmedOrder = repairOrderService.getById(order.getId());
        assertEquals("COMPLETED", confirmedOrder.getStatus());
    }

    @Test
    @DisplayName("测试取消工单")
    void testCancelOrder() {
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);

        repairOrderService.cancelOrder(order.getId(), "不需要维修了");

        RepairOrder cancelledOrder = repairOrderService.getById(order.getId());
        assertEquals("CANCELLED", cancelledOrder.getStatus());
        assertEquals("不需要维修了", cancelledOrder.getCancelReason());
    }

    @Test
    @DisplayName("测试查找待分配工单")
    void testFindPendingAssign() {
        List<RepairOrder> orders = repairOrderService.findPendingAssign();
        assertTrue(orders.isEmpty() || orders.stream().allMatch(o -> "PENDING_ASSIGN".equals(o.getStatus())));
    }

    @Test
    @DisplayName("测试查找学生工单")
    void testFindByStudentId() {
        List<RepairOrder> orders = repairOrderService.findByStudentId(2L);
        assertTrue(orders.isEmpty() || orders.stream().allMatch(o -> 2L.equals(o.getStudentId())));
    }

    @Test
    @DisplayName("测试查找修理工工单")
    void testFindByRepairmanId() {
        List<RepairOrder> orders = repairOrderService.findByRepairmanId(3L);
        assertTrue(orders.isEmpty() || orders.stream().allMatch(o -> 3L.equals(o.getRepairmanId())));
    }

    @Test
    @DisplayName("测试查找待处理工单")
    void testFindPendingTreat() {
        // 创建并分配工单
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setFaultTypeId(1L);
        dto.setDescription("测试工单");
        dto.setBuilding("1号楼");
        dto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, dto);
        repairOrderService.assignOrder(order.getId(), 3L);

        List<RepairOrder> orders = repairOrderService.findPendingTreat(3L);
        assertFalse(orders.isEmpty());
        assertTrue(orders.stream().allMatch(o -> "PENDING_TREAT".equals(o.getStatus()) && 3L.equals(o.getRepairmanId())));
    }

    @Test
    @DisplayName("测试根据状态查找工单")
    void testFindByStatus() {
        List<RepairOrder> orders = repairOrderService.findByStatus("PENDING_ASSIGN");
        assertTrue(orders.isEmpty() || orders.stream().allMatch(o -> "PENDING_ASSIGN".equals(o.getStatus())));
    }
}