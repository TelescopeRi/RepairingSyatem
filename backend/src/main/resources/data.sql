-- ==========================================
-- data.sql - 完整数据初始化脚本（无emoji）
-- 说明：Spring Boot启动时会自动执行此脚本
-- 包含：building, fault_type, user, repair_order, evaluation, chat_message
-- ==========================================

-- 1. 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `chat_message`;
TRUNCATE TABLE `evaluation`;
TRUNCATE TABLE `repair_order`;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `fault_type`;
TRUNCATE TABLE `building`;

SET FOREIGN_KEY_CHECKS = 1;

-- 重置自增起始值
ALTER TABLE `building` AUTO_INCREMENT = 1;
ALTER TABLE `fault_type` AUTO_INCREMENT = 1;
ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE `repair_order` AUTO_INCREMENT = 1;
ALTER TABLE `evaluation` AUTO_INCREMENT = 1;
ALTER TABLE `chat_message` AUTO_INCREMENT = 1;

-- ==========================================
-- 2. 创建聊天消息表
-- ==========================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `session_id` varchar(64) NOT NULL COMMENT '会话ID',
                                `role` varchar(20) NOT NULL COMMENT '角色: user/agent',
                                `content` text NOT NULL COMMENT '消息内容',
                                `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_session_id` (`session_id`),
                                KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

-- ==========================================
-- 3. 初始化楼栋数据 (1-32号楼 + A,B,C,D楼)
-- ==========================================
INSERT INTO `building` (`id`, `name`, `status`, `is_deleted`) VALUES
                                                                  (1, '1号楼', 1, 0),
                                                                  (2, '2号楼', 1, 0),
                                                                  (3, '3号楼', 1, 0),
                                                                  (4, '4号楼', 1, 0),
                                                                  (5, '5号楼', 1, 0),
                                                                  (6, '6号楼', 1, 0),
                                                                  (7, '7号楼', 1, 0),
                                                                  (8, '8号楼', 1, 0),
                                                                  (9, '9号楼', 1, 0),
                                                                  (10, '10号楼', 1, 0),
                                                                  (11, '11号楼', 1, 0),
                                                                  (12, '12号楼', 1, 0),
                                                                  (13, '13号楼', 1, 0),
                                                                  (14, '14号楼', 1, 0),
                                                                  (15, '15号楼', 1, 0),
                                                                  (16, '16号楼', 1, 0),
                                                                  (17, '17号楼', 1, 0),
                                                                  (18, '18号楼', 1, 0),
                                                                  (19, '19号楼', 1, 0),
                                                                  (20, '20号楼', 1, 0),
                                                                  (21, '21号楼', 1, 0),
                                                                  (22, '22号楼', 1, 0),
                                                                  (23, '23号楼', 1, 0),
                                                                  (24, '24号楼', 1, 0),
                                                                  (25, '25号楼', 1, 0),
                                                                  (26, '26号楼', 1, 0),
                                                                  (27, '27号楼', 1, 0),
                                                                  (28, '28号楼', 1, 0),
                                                                  (29, '29号楼', 1, 0),
                                                                  (30, '30号楼', 1, 0),
                                                                  (31, '31号楼', 1, 0),
                                                                  (32, '32号楼', 1, 0),
                                                                  (33, 'A楼', 1, 0),
                                                                  (34, 'B楼', 1, 0),
                                                                  (35, 'C楼', 1, 0),
                                                                  (36, 'D楼', 1, 0);

-- ==========================================
-- 4. 初始化故障类型数据
-- ==========================================
INSERT INTO `fault_type` (`id`, `name`, `sort_order`, `status`, `is_deleted`) VALUES
                                                                                  (1, '水路故障（水管、水龙头、马桶）', 1, 1, 0),
                                                                                  (2, '电路故障（灯具、插座、开关）', 2, 1, 0),
                                                                                  (3, '家具损坏（床、衣柜、书桌）', 3, 1, 0),
                                                                                  (4, '门窗损坏（门锁、窗户、把手）', 4, 1, 0),
                                                                                  (5, '电器故障（空调、风扇、热水器）', 5, 1, 0),
                                                                                  (6, '网络故障（路由器、网线、WiFi）', 6, 1, 0),
                                                                                  (7, '卫浴设施（淋浴、洗手池、排水）', 7, 1, 0),
                                                                                  (8, '墙体地面（墙面开裂、地砖损坏）', 8, 1, 0),
                                                                                  (9, '公共设施（开水器、洗衣机）', 9, 1, 0),
                                                                                  (10, '智能设备（门禁、电表、水表）', 10, 1, 0),
                                                                                  (11, '安全隐患（漏电、火灾隐患）', 11, 1, 0),
                                                                                  (12, '其他故障', 12, 1, 0);

-- ==========================================
-- 5. 初始化用户数据
-- 密码统一为: 123456
-- BCrypt加密: $2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy
-- ==========================================

-- 5.1 管理员
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `role`, `status`, `create_time`, `is_deleted`) VALUES
    (1, 'admin', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '系统管理员', '13800138000', 'ADMIN', 1, NOW(), 0);

-- 5.2 维修工 (编号: 0001-0010)
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `role`, `status`, `create_time`, `specialty_ids`, `is_deleted`) VALUES
                                                                                                                                            (2, '0001', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '张建国', '13900139001', 'REPAIR', 1, NOW(), '1,2,5', 0),
                                                                                                                                            (3, '0002', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '李伟', '13900139002', 'REPAIR', 1, NOW(), '3,4,6', 0),
                                                                                                                                            (4, '0003', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '王强', '13900139003', 'REPAIR', 1, NOW(), '7,8,9', 0),
                                                                                                                                            (5, '0004', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '刘明', '13900139004', 'REPAIR', 1, NOW(), '10,11,12', 0),
                                                                                                                                            (6, '0005', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '陈华', '13900139005', 'REPAIR', 1, NOW(), '1,3,7', 0),
                                                                                                                                            (7, '0006', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '赵亮', '13900139006', 'REPAIR', 1, NOW(), '2,4,8', 0),
                                                                                                                                            (8, '0007', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '孙伟', '13900139007', 'REPAIR', 1, NOW(), '5,6,9', 0),
                                                                                                                                            (9, '0008', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '周强', '13900139008', 'REPAIR', 1, NOW(), '1,4,10', 0),
                                                                                                                                            (10, '0009', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '吴刚', '13900139009', 'REPAIR', 1, NOW(), '2,5,11', 0),
                                                                                                                                            (11, '0010', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '郑军', '13900139010', 'REPAIR', 1, NOW(), '3,6,12', 0);

-- 5.3 学生 (共25人)
-- 宿舍号统一规则：1-31号楼使用1001，A/B/C/D楼使用0101
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `role`, `status`, `create_time`, `building`, `dorm_number`, `is_deleted`) VALUES
-- 信息学院 (101) - 5人
(12, '231010101', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '张三', '18912340001', 'STUDENT', 1, NOW(), '1号楼', '1001', 0),
(13, '231010102', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '李四', '18912340002', 'STUDENT', 1, NOW(), '1号楼', '1001', 0),
(14, '231010103', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '王五', '18912340003', 'STUDENT', 1, NOW(), '2号楼', '1001', 0),
(15, '231010104', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '赵六', '18912340004', 'STUDENT', 1, NOW(), '2号楼', '1001', 0),
(16, '231010105', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小明', '18912340005', 'STUDENT', 1, NOW(), '3号楼', '1001', 0),
-- 工程学院 (102) - 5人
(17, '231020101', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小红', '18912340006', 'STUDENT', 1, NOW(), '3号楼', '1001', 0),
(18, '231020102', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小刚', '18912340007', 'STUDENT', 1, NOW(), '4号楼', '1001', 0),
(19, '231020103', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小丽', '18912340008', 'STUDENT', 1, NOW(), '4号楼', '1001', 0),
(20, '231020104', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小强', '18912340009', 'STUDENT', 1, NOW(), '5号楼', '1001', 0),
(21, '231020105', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '小芳', '18912340010', 'STUDENT', 1, NOW(), '5号楼', '1001', 0),
-- 理学院 (103) - 5人
(22, '231030101', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '张伟', '18912340011', 'STUDENT', 1, NOW(), '6号楼', '1001', 0),
(23, '231030102', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '李娜', '18912340012', 'STUDENT', 1, NOW(), '6号楼', '1001', 0),
(24, '231030103', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '王芳', '18912340013', 'STUDENT', 1, NOW(), '7号楼', '1001', 0),
(25, '231030104', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '刘洋', '18912340014', 'STUDENT', 1, NOW(), '7号楼', '1001', 0),
(26, '231030105', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '陈晨', '18912340015', 'STUDENT', 1, NOW(), '7号楼', '1001', 0),
-- 文学院 (104) - 5人
(27, '231040101', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '杨静', '18912340016', 'STUDENT', 1, NOW(), '8号楼', '1001', 0),
(28, '231040102', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '周涛', '18912340017', 'STUDENT', 1, NOW(), '9号楼', '1001', 0),
(29, '231040103', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '吴迪', '18912340018', 'STUDENT', 1, NOW(), '10号楼', '1001', 0),
(30, '231040104', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '郑爽', '18912340019', 'STUDENT', 1, NOW(), '10号楼', '1001', 0),
(31, '231040105', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '黄娟', '18912340020', 'STUDENT', 1, NOW(), '11号楼', '1001', 0),
-- 商学院 (105) - 5人（A-D楼）
(32, '231050101', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '徐凯', '18912340021', 'STUDENT', 1, NOW(), 'A楼', '0101', 0),
(33, '231050102', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '孙丽', '18912340022', 'STUDENT', 1, NOW(), 'A楼', '0101', 0),
(34, '231050103', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '朱敏', '18912340023', 'STUDENT', 1, NOW(), 'B楼', '0101', 0),
(35, '231050104', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '马超', '18912340024', 'STUDENT', 1, NOW(), 'B楼', '0101', 0),
(36, '231050105', '$2a$10$U.3mtVXs3se62R44xA3KzehoU1QoE/UZEwATULKTMSdI6eavEK2Xy', '林晨', '18912340025', 'STUDENT', 1, NOW(), 'C楼', '0101', 0);

-- ==========================================
-- 6. 初始化报修工单 (50条全部为已完成)
-- 注意：包含 repair_images 字段
-- ==========================================
INSERT INTO `repair_order` (`id`, `student_id`, `repairman_id`, `building`, `dorm_number`, `fault_type_id`, `description`, `images`, `repair_images`, `urgency`, `status`, `submit_time`, `assign_time`, `start_time`, `complete_time`, `remark`, `is_deleted`) VALUES
                                                                                                                                                                                                                                                                    (1, 12, 2, '1号楼', '1001', 1, '水龙头漏水严重，一直滴水', '/uploads/fault1.jpg', '/uploads/repair1.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), '已更换新水龙头', 0),
                                                                                                                                                                                                                                                                    (2, 13, 2, '1号楼', '1001', 2, '宿舍灯不亮，整个房间没电', NULL, '/uploads/repair2.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY), '电路检修完成，已恢复供电', 0),
                                                                                                                                                                                                                                                                    (3, 14, 3, '2号楼', '1001', 3, '床板断裂，无法睡觉', '/uploads/fault3.jpg', '/uploads/repair3.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), '更换新床板', 0),
                                                                                                                                                                                                                                                                    (4, 15, 3, '2号楼', '1001', 4, '窗户把手损坏，关不严', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), '已更换把手', 0),
                                                                                                                                                                                                                                                                    (5, 16, 4, '3号楼', '1001', 5, '空调不制冷，夏天太热', '/uploads/fault5.jpg', '/uploads/repair5.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), '添加制冷剂，清洗滤网', 0),
                                                                                                                                                                                                                                                                    (6, 17, 4, '3号楼', '1001', 6, '网络经常断线，网速慢', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), '重启路由器，已恢复正常', 0),
                                                                                                                                                                                                                                                                    (7, 18, 5, '4号楼', '1001', 7, '淋浴喷头堵塞，出水小', NULL, '/uploads/repair7.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), '已清理喷头', 0),
                                                                                                                                                                                                                                                                    (8, 19, 5, '4号楼', '1001', 8, '墙面出现裂缝，掉灰', '/uploads/fault8.jpg', '/uploads/repair8.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), '已修补墙面', 0),
                                                                                                                                                                                                                                                                    (9, 20, 6, '5号楼', '1001', 9, '开水器不出热水', NULL, NULL, 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), '已维修加热管', 0),
                                                                                                                                                                                                                                                                    (10, 21, 6, '5号楼', '1001', 10, '门禁卡无法开门', NULL, '/uploads/repair10.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), '已重新授权', 0),
                                                                                                                                                                                                                                                                    (11, 22, 7, '6号楼', '1001', 11, '插座冒火花，有烧焦味', '/uploads/fault11.jpg', '/uploads/repair11.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), '已更换插座', 0),
                                                                                                                                                                                                                                                                    (12, 23, 7, '6号楼', '1001', 12, '宿舍门锁坏了，打不开', NULL, NULL, 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), '已更换门锁', 0),
                                                                                                                                                                                                                                                                    (13, 24, 8, '7号楼', '1001', 1, '马桶堵塞，无法使用', NULL, '/uploads/repair13.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), '已疏通马桶', 0),
                                                                                                                                                                                                                                                                    (14, 25, 8, '7号楼', '1001', 2, '电灯开关损坏', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), '已更换开关', 0),
                                                                                                                                                                                                                                                                    (15, 26, 9, '7号楼', '1001', 3, '衣柜门脱落', '/uploads/fault15.jpg', '/uploads/repair15.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), '已修复衣柜门', 0),
                                                                                                                                                                                                                                                                    (16, 27, 9, '8号楼', '1001', 4, '门锁生锈，难以转动', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), '已润滑门锁', 0),
                                                                                                                                                                                                                                                                    (17, 28, 10, '9号楼', '1001', 5, '风扇转速慢', NULL, '/uploads/repair17.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), '已维修风扇', 0),
                                                                                                                                                                                                                                                                    (18, 29, 10, '10号楼', '1001', 6, 'WiFi信号弱', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), '已调整路由器位置', 0),
                                                                                                                                                                                                                                                                    (19, 30, 11, '10号楼', '1001', 7, '洗手池下水管漏水', '/uploads/fault19.jpg', '/uploads/repair19.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), '已更换下水管', 0),
                                                                                                                                                                                                                                                                    (20, 31, 11, '11号楼', '1001', 8, '地砖破裂', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), '已更换地砖', 0),
                                                                                                                                                                                                                                                                    (21, 22, 2, '12号楼', '1001', 9, '洗衣机不工作', NULL, '/uploads/repair21.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), '已维修洗衣机', 0),
                                                                                                                                                                                                                                                                    (22, 23, 3, '13号楼', '1001', 10, '电表读数异常', '/uploads/fault22.jpg', '/uploads/repair22.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), '已校准电表', 0),
                                                                                                                                                                                                                                                                    (23, 24, 4, '14号楼', '1001', 11, '疑似漏电', NULL, NULL, 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '已排查漏电隐患', 0),
                                                                                                                                                                                                                                                                    (24, 25, 5, '15号楼', '1001', 12, '天花板漏水', NULL, '/uploads/repair24.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '已修复漏水点', 0),
                                                                                                                                                                                                                                                                    (25, 26, 6, '16号楼', '1001', 1, '水压太小', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '已检修管道', 0),
                                                                                                                                                                                                                                                                    (26, 27, 7, '17号楼', '1001', 2, '电灯忽明忽暗', '/uploads/fault26.jpg', '/uploads/repair26.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '已检查电路', 0),
                                                                                                                                                                                                                                                                    (27, 28, 8, '18号楼', '1001', 3, '书桌抽屉卡住', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '已修理抽屉', 0),
                                                                                                                                                                                                                                                                    (28, 29, 9, '19号楼', '1001', 4, '窗户玻璃裂纹', NULL, '/uploads/repair28.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), '已更换玻璃', 0),
                                                                                                                                                                                                                                                                    (29, 30, 10, '20号楼', '1001', 5, '空调外机噪音大', '/uploads/fault29.jpg', '/uploads/repair29.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), '已维修外机', 0),
                                                                                                                                                                                                                                                                    (30, 31, 11, '21号楼', '1001', 6, '网络接口损坏', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), '已更换网络接口', 0),
                                                                                                                                                                                                                                                                    (31, 32, 2, 'A楼', '0101', 7, '热水器不出水', NULL, '/uploads/repair31.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), '已维修热水器', 0),
                                                                                                                                                                                                                                                                    (32, 33, 3, 'A楼', '0101', 8, '墙面发霉', '/uploads/fault32.jpg', '/uploads/repair32.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), '已处理墙面', 0),
                                                                                                                                                                                                                                                                    (33, 34, 4, 'B楼', '0101', 9, '开水机漏水', NULL, NULL, 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), '已修复开水机', 0),
                                                                                                                                                                                                                                                                    (34, 35, 5, 'B楼', '0101', 10, '门禁失灵', NULL, '/uploads/repair34.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), '已修复门禁', 0),
                                                                                                                                                                                                                                                                    (35, 36, 6, 'C楼', '0101', 11, '烟雾报警器误报', '/uploads/fault35.jpg', '/uploads/repair35.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '已调整报警器', 0),
                                                                                                                                                                                                                                                                    (36, 12, 7, '22号楼', '1001', 12, '排气扇故障', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '已更换排气扇', 0),
                                                                                                                                                                                                                                                                    (37, 13, 8, '23号楼', '1001', 1, '卫生间下水道反味', NULL, '/uploads/repair37.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '已处理下水道', 0),
                                                                                                                                                                                                                                                                    (38, 14, 9, '24号楼', '1001', 2, '插座松动', '/uploads/fault38.jpg', '/uploads/repair38.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '已固定插座', 0),
                                                                                                                                                                                                                                                                    (39, 15, 10, '25号楼', '1001', 3, '床摇晃', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), '已加固床架', 0),
                                                                                                                                                                                                                                                                    (40, 16, 11, '26号楼', '1001', 4, '猫眼损坏', NULL, '/uploads/repair40.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), '已更换猫眼', 0),
                                                                                                                                                                                                                                                                    (41, 17, 2, '27号楼', '1001', 5, '空调遥控器失灵', '/uploads/fault41.jpg', '/uploads/repair41.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), '已更换遥控器', 0),
                                                                                                                                                                                                                                                                    (42, 18, 3, '28号楼', '1001', 6, '网线接口损坏', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), '已更换网线接口', 0),
                                                                                                                                                                                                                                                                    (43, 19, 4, '29号楼', '1001', 7, '淋浴水管破裂', NULL, '/uploads/repair43.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), '已更换水管', 0),
                                                                                                                                                                                                                                                                    (44, 20, 5, '30号楼', '1001', 8, '地砖空鼓', '/uploads/fault44.jpg', '/uploads/repair44.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), '已重新铺设地砖', 0),
                                                                                                                                                                                                                                                                    (45, 21, 6, '31号楼', '1001', 9, '开水机水温不够', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), '已调整温控', 0),
                                                                                                                                                                                                                                                                    (46, 22, 7, 'D楼', '0101', 10, '智能水表故障', NULL, '/uploads/repair46.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), '已更换水表', 0),
                                                                                                                                                                                                                                                                    (47, 23, 8, 'D楼', '0101', 11, '电线老化', '/uploads/fault47.jpg', '/uploads/repair47.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '已更换电线', 0),
                                                                                                                                                                                                                                                                    (48, 24, 9, 'D楼', '0101', 12, '门把手脱落', NULL, NULL, 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '已固定门把手', 0),
                                                                                                                                                                                                                                                                    (49, 25, 10, 'C楼', '0101', 1, '水龙头出水小', NULL, '/uploads/repair49.jpg', 'NORMAL', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), '已清理滤网', 0),
                                                                                                                                                                                                                                                                    (50, 26, 11, 'C楼', '0101', 2, '电灯闪烁', '/uploads/fault50.jpg', '/uploads/repair50.jpg', 'URGENT', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), '已更换灯管', 0);

-- ==========================================
-- 7. 初始化评价数据 (50条工单对应的评价)
-- ==========================================
INSERT INTO `evaluation` (`id`, `order_id`, `student_id`, `rating`, `comment`, `create_time`, `is_deleted`) VALUES
                                                                                                                (1, 1, 12, 5, '维修非常及时，师傅态度很好！', DATE_SUB(NOW(), INTERVAL 27 DAY), 0),
                                                                                                                (2, 2, 13, 4, '修好了，速度还可以', DATE_SUB(NOW(), INTERVAL 26 DAY), 0),
                                                                                                                (3, 3, 14, 5, '很满意，解决了我的大问题', DATE_SUB(NOW(), INTERVAL 25 DAY), 0),
                                                                                                                (4, 4, 15, 4, '还行，就是等了一天', DATE_SUB(NOW(), INTERVAL 24 DAY), 0),
                                                                                                                (5, 5, 16, 5, '空调修好了，现在很凉快', DATE_SUB(NOW(), INTERVAL 23 DAY), 0),
                                                                                                                (6, 6, 17, 3, '网络修好了，但速度还是有点慢', DATE_SUB(NOW(), INTERVAL 22 DAY), 0),
                                                                                                                (7, 7, 18, 5, '喷头清理后出水很大', DATE_SUB(NOW(), INTERVAL 21 DAY), 0),
                                                                                                                (8, 8, 19, 4, '墙面修补得不错', DATE_SUB(NOW(), INTERVAL 20 DAY), 0),
                                                                                                                (9, 9, 20, 5, '开水器修好了，感谢师傅', DATE_SUB(NOW(), INTERVAL 19 DAY), 0),
                                                                                                                (10, 10, 21, 4, '门禁已修复', DATE_SUB(NOW(), INTERVAL 18 DAY), 0),
                                                                                                                (11, 11, 22, 5, '插座换好了，安全第一', DATE_SUB(NOW(), INTERVAL 17 DAY), 0),
                                                                                                                (12, 12, 23, 5, '门锁已更换，很满意', DATE_SUB(NOW(), INTERVAL 16 DAY), 0),
                                                                                                                (13, 13, 24, 4, '马桶疏通了', DATE_SUB(NOW(), INTERVAL 15 DAY), 0),
                                                                                                                (14, 14, 25, 5, '开关已换好', DATE_SUB(NOW(), INTERVAL 14 DAY), 0),
                                                                                                                (15, 15, 26, 4, '衣柜门修好了', DATE_SUB(NOW(), INTERVAL 13 DAY), 0),
                                                                                                                (16, 16, 27, 5, '门锁现在很顺滑', DATE_SUB(NOW(), INTERVAL 12 DAY), 0),
                                                                                                                (17, 17, 28, 4, '风扇修好了，风很大', DATE_SUB(NOW(), INTERVAL 11 DAY), 0),
                                                                                                                (18, 18, 29, 5, 'WiFi信号强多了', DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
                                                                                                                (19, 19, 30, 4, '下水管换好了', DATE_SUB(NOW(), INTERVAL 9 DAY), 0),
                                                                                                                (20, 20, 31, 5, '地砖换得很及时', DATE_SUB(NOW(), INTERVAL 8 DAY), 0),
                                                                                                                (21, 21, 22, 5, '洗衣机修好了', DATE_SUB(NOW(), INTERVAL 7 DAY), 0),
                                                                                                                (22, 22, 23, 4, '电表已校准', DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
                                                                                                                (23, 23, 24, 5, '漏电问题解决了', DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
                                                                                                                (24, 24, 25, 4, '天花板不漏水了', DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
                                                                                                                (25, 25, 26, 5, '水压恢复正常', DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
                                                                                                                (26, 26, 27, 4, '电灯不闪了', DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
                                                                                                                (27, 27, 28, 5, '抽屉修好了', DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
                                                                                                                (28, 28, 29, 5, '玻璃已更换', NOW(), 0),
                                                                                                                (29, 29, 30, 4, '空调噪音小了', DATE_ADD(NOW(), INTERVAL 1 DAY), 0),
                                                                                                                (30, 30, 31, 5, '网络接口已修复', DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
                                                                                                                (31, 31, 32, 5, '热水器修好了', DATE_SUB(NOW(), INTERVAL 22 DAY), 0),
                                                                                                                (32, 32, 33, 4, '墙面处理好了', DATE_SUB(NOW(), INTERVAL 17 DAY), 0),
                                                                                                                (33, 33, 34, 5, '开水机不漏水了', DATE_SUB(NOW(), INTERVAL 12 DAY), 0),
                                                                                                                (34, 34, 35, 4, '门禁恢复正常', DATE_SUB(NOW(), INTERVAL 7 DAY), 0),
                                                                                                                (35, 35, 36, 5, '报警器不误报了', DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
                                                                                                                (36, 36, 12, 4, '排气扇换好了', DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
                                                                                                                (37, 37, 13, 5, '下水道没味道了', DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
                                                                                                                (38, 38, 14, 4, '插座固定好了', DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
                                                                                                                (39, 39, 15, 5, '床不晃了', NOW(), 0),
                                                                                                                (40, 40, 16, 4, '猫眼已更换', DATE_ADD(NOW(), INTERVAL 1 DAY), 0),
                                                                                                                (41, 41, 17, 5, '遥控器换好了', DATE_SUB(NOW(), INTERVAL 25 DAY), 0),
                                                                                                                (42, 42, 18, 4, '网线接口已换', DATE_SUB(NOW(), INTERVAL 19 DAY), 0),
                                                                                                                (43, 43, 19, 5, '水管已更换', DATE_SUB(NOW(), INTERVAL 15 DAY), 0),
                                                                                                                (44, 44, 20, 4, '地砖铺好了', DATE_SUB(NOW(), INTERVAL 11 DAY), 0),
                                                                                                                (45, 45, 21, 5, '开水机水温正常了', DATE_SUB(NOW(), INTERVAL 9 DAY), 0),
                                                                                                                (46, 46, 22, 4, '水表已更换', DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
                                                                                                                (47, 47, 23, 5, '电线已更换', DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
                                                                                                                (48, 48, 24, 4, '门把手固定了', DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
                                                                                                                (49, 49, 25, 5, '水龙头出水正常', DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
                                                                                                                (50, 50, 26, 5, '电灯不闪了', NOW(), 0);

-- ==========================================
-- 8. 初始化聊天消息数据（无emoji）
-- ==========================================
INSERT INTO `chat_message` (`id`, `session_id`, `role`, `content`, `timestamp`, `created_at`) VALUES
                                                                                                  (1, 'default_admin', 'user', '帮我总结近30天报修情况', '2026-06-09 20:59:18', '2026-06-09 20:59:18'),
                                                                                                  (2, 'default_admin', 'agent', '好的！根据系统实时数据，我来为您总结近30天的报修情况如下：

---

[统计] 近30天报修情况总结（截至2026年6月9日）

1. 总体工单量
- 总工单数：50单
- 全部已完成，无待处理、进行中或取消工单。

2. 近7天报修趋势（每日新增）
| 日期     | 报修单数 |
|----------|----------|
| 06-03    | 2        |
| 06-04    | 3        |
| 06-05    | 2        |
| 06-06    | 3        |
| 06-07    | 3        |
| 06-08    | 1        |
| 06-09    | 5        |

- 近7天共新增 19单，其中今天（06-09）新增最多（5单）。
- 整体趋势平稳，偶有小幅波动。

3. 故障类型分布（前5类）
| 故障类型     | 数量 |
|--------------|------|
| 水路故障     | 6    |
| 家具损坏     | 6    |
| 电器故障     | 6    |
| 门窗损坏     | 5    |
| 网络故障     | 4    |

- 水路、家具、电器维修是主要报修类型，合计占比超过50%。

4. 楼栋报修分布（前5）
| 楼栋   | 报修数 |
|--------|--------|
| 31号楼 | 6      |
| 7号楼  | 3      |
| C楼    | 3      |
| D楼    | 3      |

- 31号楼报修最多，需重点关注。

5. 维修工工作概况
- 所有10名维修工均在线，且当前无处理中工单。
- 张建国完成最多（8单），李伟次之（7单），其余维修工各完成5单。
- 整体维修效率较高，工单已全部闭环。

6. 紧急工单情况
- 待处理紧急工单：0单，说明目前无紧急报修积压。

---

[总体评价]
- 系统运行良好，所有工单均已处理完毕，无积压。
- 近30天报修量适中，主要集中在水路、家具、电器类。
- 建议对31号楼的设施进行定期巡检，以降低报修频次。

如需进一步分析某类故障、某位维修工绩效或某栋楼的详细工单，请随时告诉我！', '2026-06-09 20:59:25', '2026-06-09 20:59:25'),
                                                                                                  (3, 'default_admin', 'user', '总结近30天维修情况', '2026-06-09 21:05:52', '2026-06-09 21:05:52'),
                                                                                                  (4, 'default_admin', 'agent', '好的，管理员您好！根据系统提供的近7天报修趋势数据，我来为您总结近期的维修情况。

需要说明的是，当前系统仅提供了近7天（2026年6月3日至6月9日）的报修趋势数据，因此我无法准确统计完整的近30天情况。以下是根据现有数据的总结：

[趋势] 近7天报修趋势总结（2026-06-03 至 2026-06-09）

* 总报修量：近7天共产生 19单 维修工单。
* 日均报修量：平均每天约 2.7单。
* 报修波动：
    * 报修量在 6月9日（今天）达到峰值，共 5单。
    * 报修量在 6月8日 最低，仅有 1单。
* 当前状态：系统内所有历史工单（共50单）已全部完成，目前无待处理、进行中或紧急的工单，所有维修工均处于在线状态，维修资源充足。

[全景] 整体数据全景（基于所有历史工单）

* 总工单数：50单（已全部完成）
* 报修热点楼栋：31号楼（6单）报修最多，其次为7号楼、C楼、D楼（各3单）。
* 常见故障类型：水路故障、家具损坏、电器故障（各6单）最为集中。

[建议] 如果需要更精确的"近30天"分析（如月度趋势、各维修工月度绩效等），您可以考虑通过系统的数据导出功能，将过去30天的工单数据导出后，我再帮您进行详细分析。您需要我指导您如何导出数据吗？', '2026-06-09 21:05:57', '2026-06-09 21:05:57'),
                                                                                                  (5, 'default_admin', 'user', '总结修理工绩效', '2026-06-09 21:06:07', '2026-06-09 21:06:07'),
                                                                                                  (6, 'default_admin', 'agent', '好的，管理员您好！根据系统数据，我来为您总结一下维修工的绩效情况。

截至2026年6月9日，系统内共有 10名 维修工，目前全部处于在线状态，且均无待处理的工单。所有历史工单（共50单）已全部完成，没有积压情况。

以下是各位维修工的具体绩效排名（按已完成工单数从高到低）：

1. 张建国 (ID:2): 8单 (绩效最高)
2. 李伟 (ID:3): 7单
3. 王强 (ID:4): 5单
4. 刘明 (ID:5): 5单
5. 陈华 (ID:6): 5单
6. 赵亮 (ID:7): 5单
7. 孙伟 (ID:8): 5单
8. 周强 (ID:9): 5单
9. 吴刚 (ID:10): 5单
10. 郑军 (ID:11): 5单

[总结分析]
- 绩效冠军：张建国师傅以完成8单的成绩领先，李伟师傅完成7单紧随其后，两人表现突出。
- 中坚力量：其余8位师傅均完成了5单，工作量非常平均，团队整体效率稳定。
- 当前状态：所有工单均已完结，无积压，说明维修团队近期工作完成得非常出色。

如果您需要查看更详细的数据，比如每位师傅处理的具体故障类型或维修时长，请随时告诉我。', '2026-06-09 21:06:10', '2026-06-09 21:06:10'),
                                                                                                  (7, 'default_admin', 'user', '总结今天的报修情况', '2026-06-09 21:10:08', '2026-06-09 21:10:08'),
                                                                                                  (8, 'default_admin', 'agent', '好的，管理员您好！根据系统实时数据，我来为您总结今天（2026年6月9日）的报修情况：

[今日报修概况]
- 今日新增工单：5单
- 所有工单目前状态良好，无积压：待分配、待处理、进行中、待确认的工单均为 0单。
- 紧急工单：0单，今天没有需要紧急处理的报修。

[近7天趋势对比]
今天的5单是过去7天中新增最多的一天，比前几天的1-3单有明显增加，可能今天报修需求比较集中。

[维修工状态]
目前全部10位维修工（张建国、李伟、王强等）均处于在线状态，且处理中的工单为0，说明维修力量充足，可以随时接单。

[报修分布]
31号楼累计报修最多（6单），其次是7号楼、C楼、D楼（各3单）。

[总结] 今天报修量处于近一周的高点，但所有工单都已处理完毕，系统运转高效，没有积压和紧急情况。如果您需要查看今日这5单的具体楼栋或故障类型明细，我可以进一步帮您分析。', '2026-06-09 21:10:12', '2026-06-09 21:10:12');

-- ==========================================
-- 9. 验证数据
-- ==========================================
SELECT 'building' AS table_name, COUNT(*) AS count FROM building
UNION ALL
SELECT 'fault_type', COUNT(*) FROM fault_type
UNION ALL
SELECT 'user', COUNT(*) FROM user
UNION ALL
SELECT 'repair_order', COUNT(*) FROM repair_order
UNION ALL
SELECT 'evaluation', COUNT(*) FROM evaluation
UNION ALL
SELECT 'chat_message', COUNT(*) FROM chat_message;