-- ==========================================
-- 数据初始化脚本
-- ==========================================

-- 1. 清空现有数据（保留表结构）
SET FOREIGN_KEY_CHECKS = 0;  -- 暂时禁用外键检查

-- 清空表数据（按依赖顺序，先删子表再删父表）
TRUNCATE TABLE `repair_order`;      -- 如果有报修订单表
TRUNCATE TABLE `evaluation`;        -- 如果有评价表
TRUNCATE TABLE `user`;
TRUNCATE TABLE `fault_type`;
TRUNCATE TABLE `building`;

SET FOREIGN_KEY_CHECKS = 1;  -- 恢复外键检查

-- ==========================================
-- 初始化数据
-- ==========================================

-- 初始化管理员账户
REPLACE INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `role`, `status`, `create_time`)
VALUES (1, 'admin', '$2a$12$pUnjE/iTK4XztKzm2XL6ReBzGTAZSCJwFI5.ypePosvqYeV6TSOVa', '管理员', '13800138000', 'ADMIN', 1, NOW());

-- 初始化故障类型
REPLACE INTO `fault_type` (`id`, `name`, `sort_order`, `status`) VALUES
(1, '水电', 1, 1),
(2, '家具', 2, 1),
(3, '电器', 3, 1),
(4, '网络', 4, 1),
(5, '其他', 5, 1);

-- 初始化楼栋
REPLACE INTO `building` (`id`, `name`, `status`) VALUES
(1, '1号楼', 1),
(2, '2号楼', 1),
(3, '3号楼', 1),
(4, '4号楼', 1),
(5, '5号楼', 1);

-- 验证数据
SELECT 'user' as table_name, COUNT(*) as count FROM user
UNION ALL
SELECT 'fault_type', COUNT(*) FROM fault_type
UNION ALL
SELECT 'building', COUNT(*) FROM building;