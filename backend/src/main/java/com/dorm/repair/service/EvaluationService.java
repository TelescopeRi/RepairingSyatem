
package com.dorm.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.repair.dto.EvaluationDTO;
import com.dorm.repair.entity.Evaluation;

import java.util.List;

public interface EvaluationService extends IService<Evaluation> {
    
    Evaluation createEvaluation(Long studentId, EvaluationDTO dto);
    
    Evaluation findByOrderId(Long orderId);
    
    List<Evaluation> findByRepairmanId(Long repairmanId);
}
