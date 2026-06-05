
package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.repair.entity.Building;
import com.dorm.repair.mapper.BuildingMapper;
import com.dorm.repair.service.BuildingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {
    
    @Override
    public List<Building> findAllEnabled() {
        return baseMapper.selectAllEnabled();
    }
}
