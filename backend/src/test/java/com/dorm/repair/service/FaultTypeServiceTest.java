package com.dorm.repair.service;

import com.dorm.repair.entity.FaultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("故障类型服务测试")
class FaultTypeServiceTest {

    @Autowired
    private FaultTypeService faultTypeService;

    @Test
    @DisplayName("测试获取所有故障类型")
    void testList() {
        List<FaultType> faultTypes = faultTypeService.list();
        assertFalse(faultTypes.isEmpty());
        assertTrue(faultTypes.size() >= 4); // 初始数据有4个故障类型
    }

    @Test
    @DisplayName("测试添加故障类型")
    void testSave() {
        FaultType faultType = new FaultType();
        faultType.setName("门窗维修");
        faultType.setSortOrder(5);
        faultType.setStatus(1);

        faultTypeService.save(faultType);

        assertNotNull(faultType.getId());
        FaultType saved = faultTypeService.getById(faultType.getId());
        assertEquals("门窗维修", saved.getName());
        assertEquals(5, saved.getSortOrder());
        assertEquals(1, saved.getStatus());
    }

    @Test
    @DisplayName("测试更新故障类型")
    void testUpdateById() {
        FaultType faultType = new FaultType();
        faultType.setName("测试类型");
        faultType.setSortOrder(10);
        faultType.setStatus(1);
        faultTypeService.save(faultType);

        faultType.setName("更新后的类型");
        faultType.setSortOrder(15);
        faultType.setStatus(0);
        faultTypeService.updateById(faultType);

        FaultType updated = faultTypeService.getById(faultType.getId());
        assertEquals("更新后的类型", updated.getName());
        assertEquals(15, updated.getSortOrder());
        assertEquals(0, updated.getStatus());
    }

    @Test
    @DisplayName("测试删除故障类型")
    void testRemoveById() {
        FaultType faultType = new FaultType();
        faultType.setName("临时类型");
        faultType.setSortOrder(99);
        faultType.setStatus(1);
        faultTypeService.save(faultType);

        Long id = faultType.getId();
        faultTypeService.removeById(id);

        assertNull(faultTypeService.getById(id));
    }

    @Test
    @DisplayName("测试根据ID查找故障类型")
    void testGetById() {
        FaultType faultType = faultTypeService.getById(1L);
        assertNotNull(faultType);
        assertEquals("水电维修", faultType.getName());
    }

    @Test
    @DisplayName("测试查找不存在的故障类型")
    void testGetByIdNotFound() {
        FaultType faultType = faultTypeService.getById(999L);
        assertNull(faultType);
    }
}