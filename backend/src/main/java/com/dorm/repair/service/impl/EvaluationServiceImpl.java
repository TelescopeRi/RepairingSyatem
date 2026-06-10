package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.repair.dto.EvaluationDTO;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.mapper.EvaluationMapper;
import com.dorm.repair.mapper.RepairOrderMapper;
import com.dorm.repair.service.EvaluationService;
import com.dorm.repair.service.RepairOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Resource
    private RepairOrderService repairOrderService;

    @Override
    @Transactional
    public Evaluation createEvaluation(Long studentId, EvaluationDTO dto) {
        // 1. 检查工单是否存在
        RepairOrder order = repairOrderService.getById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        // 2. 检查是否是当前学生的工单
        if (!order.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权评价此工单");
        }

        // 3. 检查工单状态是否允许评价（待确认和已完成状态都可以评价）
        if (!"PENDING_CONFIRM".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("工单状态不允许评价");
        }

        // 4. 检查是否已经评价过
        Evaluation existing = findByOrderId(dto.getOrderId());
        if (existing != null) {
            throw new RuntimeException("此工单已评价");
        }

        // 5. 创建评价记录
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setStudentId(studentId);
        evaluation.setRating(dto.getRating());
        evaluation.setComment(dto.getComment());
        evaluation.setCreateTime(LocalDateTime.now());

        baseMapper.insert(evaluation);

        return evaluation;
    }

    @Override
    public Evaluation findByOrderId(Long orderId) {
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<Evaluation> findByRepairmanId(Long repairmanId) {
        return baseMapper.selectByRepairmanId(repairmanId);
    }
}
