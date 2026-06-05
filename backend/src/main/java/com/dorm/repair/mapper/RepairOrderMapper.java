package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper  // 添加这个注解
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    List<RepairOrder> selectByStudentId(Long studentId);

    List<RepairOrder> selectByRepairmanId(Long repairmanId);

    List<RepairOrder> selectByStatus(String status);

    List<RepairOrder> selectByBuilding(String building);

    List<RepairOrder> selectByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<RepairOrder> selectPendingAssignTimeout(LocalDateTime timeoutTime);

    List<RepairOrder> selectPendingTreatTimeout(LocalDateTime timeoutTime);

    List<RepairOrder> selectPendingConfirmTimeout(LocalDateTime timeoutTime);
}