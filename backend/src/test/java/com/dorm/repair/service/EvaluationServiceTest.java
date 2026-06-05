package com.dorm.repair.service;

import com.dorm.repair.dto.EvaluationDTO;
import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.RepairOrder;
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
@DisplayName("评价服务测试")
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private RepairOrderService repairOrderService;

    @Test
    @DisplayName("测试创建评价")
    void testCreateEvaluation() {
        // 先创建一个完整的工单流程
        RepairOrderDTO orderDto = new RepairOrderDTO();
        orderDto.setFaultTypeId(1L);
        orderDto.setDescription("测试工单");
        orderDto.setBuilding("1号楼");
        orderDto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, orderDto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());
        repairOrderService.completeRepair(order.getId(), "已修复");
        repairOrderService.confirmOrder(order.getId());

        // 创建评价
        EvaluationDTO dto = new EvaluationDTO();
        dto.setOrderId(order.getId());
        dto.setRating(5);
        dto.setComment("维修很及时，服务态度很好");

        Evaluation evaluation = evaluationService.createEvaluation(dto);

        assertNotNull(evaluation);
        assertEquals(order.getId(), evaluation.getOrderId());
        assertEquals(5, evaluation.getRating());
        assertEquals("维修很及时，服务态度很好", evaluation.getComment());
    }

    @Test
    @DisplayName("测试创建评价-工单不存在")
    void testCreateEvaluationOrderNotFound() {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setOrderId(999L);
        dto.setRating(5);

        assertThrows(RuntimeException.class, () -> evaluationService.createEvaluation(dto));
    }

    @Test
    @DisplayName("测试创建评价-工单未完成")
    void testCreateEvaluationOrderNotCompleted() {
        // 创建一个未完成的工单
        RepairOrderDTO orderDto = new RepairOrderDTO();
        orderDto.setFaultTypeId(1L);
        orderDto.setDescription("测试工单");
        orderDto.setBuilding("1号楼");
        orderDto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, orderDto);

        EvaluationDTO dto = new EvaluationDTO();
        dto.setOrderId(order.getId());
        dto.setRating(5);

        assertThrows(RuntimeException.class, () -> evaluationService.createEvaluation(dto));
    }

    @Test
    @DisplayName("测试创建评价-已评价")
    void testCreateEvaluationAlreadyExists() {
        // 创建完整工单流程
        RepairOrderDTO orderDto = new RepairOrderDTO();
        orderDto.setFaultTypeId(1L);
        orderDto.setDescription("测试工单");
        orderDto.setBuilding("1号楼");
        orderDto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, orderDto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());
        repairOrderService.completeRepair(order.getId(), "已修复");
        repairOrderService.confirmOrder(order.getId());

        // 第一次评价
        EvaluationDTO dto1 = new EvaluationDTO();
        dto1.setOrderId(order.getId());
        dto1.setRating(4);
        evaluationService.createEvaluation(dto1);

        // 第二次评价应该失败
        EvaluationDTO dto2 = new EvaluationDTO();
        dto2.setOrderId(order.getId());
        dto2.setRating(5);

        assertThrows(RuntimeException.class, () -> evaluationService.createEvaluation(dto2));
    }

    @Test
    @DisplayName("测试根据工单ID查找评价")
    void testFindByOrderId() {
        // 创建完整工单流程并评价
        RepairOrderDTO orderDto = new RepairOrderDTO();
        orderDto.setFaultTypeId(1L);
        orderDto.setDescription("测试工单");
        orderDto.setBuilding("1号楼");
        orderDto.setDormNumber("101");
        RepairOrder order = repairOrderService.createOrder(2L, orderDto);
        repairOrderService.assignOrder(order.getId(), 3L);
        repairOrderService.startRepair(order.getId());
        repairOrderService.completeRepair(order.getId(), "已修复");
        repairOrderService.confirmOrder(order.getId());

        EvaluationDTO dto = new EvaluationDTO();
        dto.setOrderId(order.getId());
        dto.setRating(5);
        evaluationService.createEvaluation(dto);

        // 查找评价
        Evaluation evaluation = evaluationService.findByOrderId(order.getId());
        assertNotNull(evaluation);
        assertEquals(5, evaluation.getRating());
    }

    @Test
    @DisplayName("测试查找不存在的评价")
    void testFindByOrderIdNotFound() {
        Evaluation evaluation = evaluationService.findByOrderId(999L);
        assertNull(evaluation);
    }
}