package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.Building;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
    
    @Select("SELECT * FROM building WHERE status = 1 AND is_deleted = 0 ORDER BY name")
    List<Building> selectAllEnabled();
}
