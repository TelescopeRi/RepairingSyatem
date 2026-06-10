package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.repair.controller.WebSocketController;
import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.RepairOrderMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.RepairOrderService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    @Lazy
    private WebSocketController webSocketController;

    @Override
    @Transactional
    public RepairOrder createOrder(Long studentId, RepairOrderDTO dto) {
        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        RepairOrder order = new RepairOrder();
        order.setStudentId(studentId);
        order.setFaultTypeId(dto.getFaultTypeId());
        order.setDescription(dto.getDescription());
        order.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        order.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : "NORMAL");
        order.setBuilding(dto.getBuilding() != null ? dto.getBuilding() : student.getBuilding());
        order.setDormNumber(dto.getDormNumber() != null ? dto.getDormNumber() : student.getDormNumber());
        order.setStatus("PENDING_ASSIGN");
        order.setSubmitTime(LocalDateTime.now());

        baseMapper.insert(order);
        
        // 发送新工单消息
        webSocketController.sendNewOrder(order);
        
        return order;
    }

    @Override
    public List<RepairOrder> findByStudentId(Long studentId) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        wrapper.eq("is_deleted", 0);
        wrapper.orderByDesc("submit_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<RepairOrder> findByRepairmanId(Long repairmanId) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("repairman_id", repairmanId);
        wrapper.eq("is_deleted", 0);
        wrapper.orderByDesc("submit_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<RepairOrder> findPendingAssign() {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_ASSIGN");
        wrapper.eq("is_deleted", 0);
        wrapper.orderByDesc("urgency").orderByDesc("submit_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<RepairOrder> findPendingTreat(Long repairmanId) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("repairman_id", repairmanId);
        wrapper.eq("status", "PENDING_TREAT");
        wrapper.eq("is_deleted", 0);
        wrapper.orderByDesc("urgency").orderByDesc("assign_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void assignOrder(Long orderId, Long repairmanId) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (!"PENDING_ASSIGN".equals(order.getStatus())) {
            throw new RuntimeException("工单状态不允许分配");
        }

        order.setRepairmanId(repairmanId);
        order.setStatus("PENDING_TREAT");
        order.setAssignTime(LocalDateTime.now());
        updateById(order);
        
        // 发送分配消息给修理工
        webSocketController.sendOrderAssigned(order, repairmanId);
    }

    @Override
    @Transactional
    public void startRepair(Long orderId) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (!"PENDING_TREAT".equals(order.getStatus())) {
            throw new RuntimeException("工单状态不允许开始维修");
        }

        order.setStatus("IN_PROGRESS");
        order.setStartTime(LocalDateTime.now());
        updateById(order);
        
        // 发送状态变更消息
        webSocketController.sendOrderStatusChange(orderId, "IN_PROGRESS");
    }

    @Override
    @Transactional
    public void completeRepair(Long orderId, String remark, java.util.List<String> images) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new RuntimeException("工单状态不允许完成");
        }

        order.setStatus("COMPLETED");
        order.setRemark(remark);
        order.setCompleteTime(LocalDateTime.now());

        if (images != null && !images.isEmpty()) {
            // 过滤空字符串
            java.util.List<String> validImages = images.stream()
                    .filter(img -> img != null && !img.trim().isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            if (!validImages.isEmpty()) {
                order.setRepairImages(String.join(",", validImages));
            }
        }

        updateById(order);
        
        // 发送状态变更消息
        webSocketController.sendOrderStatusChange(orderId, "COMPLETED");
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        String status = order.getStatus();
        if (!"PENDING_ASSIGN".equals(status) && !"PENDING_TREAT".equals(status) && !"IN_PROGRESS".equals(status)) {
            throw new RuntimeException("工单状态不允许取消");
        }

        order.setStatus("CANCELLED");
        order.setCancelReason(reason);
        updateById(order);
    }

    @Override
    public List<RepairOrder> findByStatus(String status) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<RepairOrder> list() {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectList(wrapper);
    }
}
