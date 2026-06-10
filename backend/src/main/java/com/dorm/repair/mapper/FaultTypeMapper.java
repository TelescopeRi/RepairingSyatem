package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.FaultType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FaultTypeMapper extends BaseMapper<FaultType> {
    
    @Select("SELECT * FROM fault_type WHERE status = 1 AND is_deleted = 0 ORDER BY sort_order")
    List<FaultType> selectAllEnabled();
}
