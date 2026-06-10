
package com.dorm.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    
    /**
     * 保存故障类型（如果已删除则恢复）
     */
    @Override
    public boolean save(FaultType entity) {
        // 检查是否存在已删除的同名故障类型
        FaultType deletedFaultType = findDeletedByName(entity.getName());
        if (deletedFaultType != null) {
            // 恢复已删除的故障类型
            deletedFaultType.setName(entity.getName());
            deletedFaultType.setSortOrder(entity.getSortOrder());
            deletedFaultType.setStatus(entity.getStatus());
            deletedFaultType.setIsDeleted(0);
            return baseMapper.updateById(deletedFaultType) > 0;
        }
        
        // 检查是否存在同名的未删除故障类型
        FaultType existing = findByName(entity.getName());
        if (existing != null) {
            throw new RuntimeException("故障类型名称已存在");
        }
        
        return super.save(entity);
    }
    
    /**
     * 查询未删除的故障类型
     */
    public FaultType findByName(String name) {
        QueryWrapper<FaultType> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_deleted", 0);
        return baseMapper.selectOne(wrapper);
    }
    
    /**
     * 查询已删除的故障类型
     */
    private FaultType findDeletedByName(String name) {
        QueryWrapper<FaultType> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_deleted", 1);
        return baseMapper.selectOne(wrapper);
    }
}
