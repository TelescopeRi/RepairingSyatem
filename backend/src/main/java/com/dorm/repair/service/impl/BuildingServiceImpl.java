
package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    
    /**
     * 保存楼栋（如果已删除则恢复）
     */
    @Override
    public boolean save(Building entity) {
        // 检查是否存在已删除的同名楼栋
        Building deletedBuilding = findDeletedByName(entity.getName());
        if (deletedBuilding != null) {
            // 恢复已删除的楼栋
            deletedBuilding.setName(entity.getName());
            deletedBuilding.setStatus(entity.getStatus());
            deletedBuilding.setIsDeleted(0);
            return baseMapper.updateById(deletedBuilding) > 0;
        }
        
        // 检查是否存在同名的未删除楼栋
        Building existing = findByName(entity.getName());
        if (existing != null) {
            throw new RuntimeException("楼栋名称已存在");
        }
        
        return super.save(entity);
    }
    
    /**
     * 查询未删除的楼栋
     */
    public Building findByName(String name) {
        QueryWrapper<Building> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectOne(wrapper);
    }
    
    /**
     * 查询已删除的楼栋
     */
    private Building findDeletedByName(String name) {
        QueryWrapper<Building> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_deleted", 1);
        return baseMapper.selectOne(wrapper);
    }
}
