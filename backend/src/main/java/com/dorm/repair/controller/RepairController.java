package com.dorm.repair.controller;

import com.dorm.repair.dto.UpdateOrderStatusDTO;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.EvaluationService;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.vo.EvaluationVO;
import com.dorm.repair.vo.RepairOrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repair")
public class RepairController {
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Resource
    private EvaluationService evaluationService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @GetMapping("/orders/pending")
    public List<RepairOrderVO> getPendingOrders(Authentication auth) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        List<RepairOrder> orders = repairOrderService.findPendingTreat(userId);
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders")
    public List<RepairOrderVO> getMyOrders(Authentication auth) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        List<RepairOrder> orders = repairOrderService.findByRepairmanId(userId);
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders/{id}")
    public RepairOrderVO getOrderDetail(Authentication auth, @PathVariable Long id) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权查看此工单");
        }
        
        return convertToVO(order);
    }
    
    @PostMapping("/orders/{id}/start")
    public void startRepair(Authentication auth, @PathVariable Long id) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权操作此工单");
        }
        
        repairOrderService.startRepair(id);
    }
    
    @PostMapping("/orders/{id}/complete")
    public void completeRepair(Authentication auth, @PathVariable Long id, @RequestBody UpdateOrderStatusDTO dto) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权操作此工单");
        }
        
        repairOrderService.completeRepair(id, dto.getRemark());
    }
    
    @GetMapping("/orders/{id}/evaluation")
    public EvaluationVO getEvaluation(Authentication auth, @PathVariable Long id) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权查看此工单");
        }
        
        Evaluation evaluation = evaluationService.findByOrderId(id);
        if (evaluation == null) {
            return null;
        }
        
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setOrderId(evaluation.getOrderId());
        vo.setRating(evaluation.getRating());
        vo.setComment(evaluation.getComment());
        vo.setCreateTime(evaluation.getCreateTime());
        return vo;
    }
    
    private List<RepairOrderVO> convertToVOList(List<RepairOrder> orders) {
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    private RepairOrderVO convertToVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(order.getId());
        vo.setBuilding(order.getBuilding());
        vo.setDormNumber(order.getDormNumber());
        vo.setDescription(order.getDescription());
        vo.setUrgency(order.getUrgency());
        vo.setStatus(order.getStatus());
        vo.setStatusColor(getStatusColor(order.getStatus()));
        vo.setSubmitTime(order.getSubmitTime());
        vo.setAssignTime(order.getAssignTime());
        vo.setStartTime(order.getStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setRemark(order.getRemark());
        
        if (order.getImages() != null) {
            vo.setImages(Arrays.asList(order.getImages().split(",")));
        }
        
        FaultType faultType = faultTypeMapper.selectById(order.getFaultTypeId());
        vo.setFaultTypeName(faultType != null ? faultType.getName() : "未知");
        
        User student = userMapper.selectById(order.getStudentId());
        vo.setStudentName(student != null ? student.getRealName() : "");
        
        return vo;
    }
    
    private String getStatusColor(String status) {
        switch (status) {
            case "PENDING_ASSIGN": return "gray";
            case "PENDING_TREAT": return "blue";
            case "IN_PROGRESS": return "orange";
            case "PENDING_CONFIRM": return "purple";
            case "COMPLETED": return "green";
            case "CANCELLED": return "red";
            default: return "gray";
        }
    }
}
