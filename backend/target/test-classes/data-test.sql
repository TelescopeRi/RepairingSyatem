-- 初始化故障类型
INSERT INTO fault_type (name, sort_order, status) VALUES ('水电维修', 1, 1);
INSERT INTO fault_type (name, sort_order, status) VALUES ('家电维修', 2, 1);
INSERT INTO fault_type (name, sort_order, status) VALUES ('家具维修', 3, 1);
INSERT INTO fault_type (name, sort_order, status) VALUES ('网络维修', 4, 1);

-- 初始化楼栋
INSERT INTO building (name, status) VALUES ('1号楼', 1);
INSERT INTO building (name, status) VALUES ('2号楼', 1);
INSERT INTO building (name, status) VALUES ('3号楼', 1);

-- 初始化管理员（密码：123456）
INSERT INTO user (username, password, real_name, phone, role, status, create_time) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '管理员', '13800138000', 'ADMIN', 1, CURRENT_TIMESTAMP);

-- 初始化测试学生（密码：123456）
INSERT INTO user (username, password, real_name, phone, role, status, building, dorm_number, create_time) 
VALUES ('2021001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '张三', '13800138001', 'STUDENT', 1, '1号楼', '101', CURRENT_TIMESTAMP);

-- 初始化测试修理工（密码：123456）
INSERT INTO user (username, password, real_name, phone, role, status, building, create_time) 
VALUES ('WX202401010001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '李师傅', '13800138002', 'REPAIR', 1, '1号楼', CURRENT_TIMESTAMP);