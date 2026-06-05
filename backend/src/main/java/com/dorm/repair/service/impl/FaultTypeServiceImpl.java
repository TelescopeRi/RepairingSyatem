
package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.service.FaultTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaultTypeServiceImpl extends ServiceImpl<FaultTypeMapper, FaultType> implements FaultTypeService {
    
    @Override
    public List<FaultType> findAllEnabled() {
        return baseMapper.selectAllEnabled();
    }
}
