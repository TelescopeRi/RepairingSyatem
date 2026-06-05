package com.dorm.repair.service;

import com.dorm.repair.entity.Building;
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
@DisplayName("楼栋服务测试")
class BuildingServiceTest {

    @Autowired
    private BuildingService buildingService;

    @Test
    @DisplayName("测试获取所有楼栋")
    void testList() {
        List<Building> buildings = buildingService.list();
        assertFalse(buildings.isEmpty());
        assertTrue(buildings.size() >= 3); // 初始数据有3个楼栋
    }

    @Test
    @DisplayName("测试添加楼栋")
    void testSave() {
        Building building = new Building();
        building.setName("4号楼");
        building.setStatus(1);

        buildingService.save(building);

        assertNotNull(building.getId());
        Building saved = buildingService.getById(building.getId());
        assertEquals("4号楼", saved.getName());
        assertEquals(1, saved.getStatus());
    }

    @Test
    @DisplayName("测试更新楼栋")
    void testUpdateById() {
        Building building = new Building();
        building.setName("测试楼");
        building.setStatus(1);
        buildingService.save(building);

        building.setName("更新后的楼栋");
        building.setStatus(0);
        buildingService.updateById(building);

        Building updated = buildingService.getById(building.getId());
        assertEquals("更新后的楼栋", updated.getName());
        assertEquals(0, updated.getStatus());
    }

    @Test
    @DisplayName("测试删除楼栋")
    void testRemoveById() {
        Building building = new Building();
        building.setName("临时楼");
        building.setStatus(1);
        buildingService.save(building);

        Long id = building.getId();
        buildingService.removeById(id);

        assertNull(buildingService.getById(id));
    }

    @Test
    @DisplayName("测试根据ID查找楼栋")
    void testGetById() {
        Building building = buildingService.getById(1L);
        assertNotNull(building);
        assertEquals("1号楼", building.getName());
    }

    @Test
    @DisplayName("测试查找不存在的楼栋")
    void testGetByIdNotFound() {
        Building building = buildingService.getById(999L);
        assertNull(building);
    }
}