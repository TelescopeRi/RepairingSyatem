
package com.dorm.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.repair.entity.Building;

import java.util.List;

public interface BuildingService extends IService<Building> {
    
    List<Building> findAllEnabled();
}
