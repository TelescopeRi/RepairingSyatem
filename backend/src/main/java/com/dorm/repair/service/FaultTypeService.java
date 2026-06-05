
package com.dorm.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.repair.entity.FaultType;

import java.util.List;

public interface FaultTypeService extends IService<FaultType> {
    
    List<FaultType> findAllEnabled();
}
