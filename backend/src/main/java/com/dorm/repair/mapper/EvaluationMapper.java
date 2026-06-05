package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    @Select("SELECT * FROM evaluation WHERE student_id = #{studentId} ORDER BY create_time DESC")
    List<Evaluation> selectByStudentId(Long studentId);

    @Select("SELECT * FROM evaluation WHERE order_id = #{orderId}")
    List<Evaluation> selectByOrderId(Long orderId);

    @Select("SELECT e.* FROM evaluation e JOIN repair_order o ON e.order_id = o.id WHERE o.repairman_id = #{repairmanId} ORDER BY e.create_time DESC")
    List<Evaluation> selectByRepairmanId(Long repairmanId);
}