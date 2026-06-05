-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status INT NOT NULL DEFAULT 1,
    building VARCHAR(50),
    dorm_number VARCHAR(20),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 故障类型表
CREATE TABLE IF NOT EXISTS fault_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    status INT NOT NULL DEFAULT 1
);

-- 楼栋表
CREATE TABLE IF NOT EXISTS building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    status INT NOT NULL DEFAULT 1
);

-- 工单反
CREATE TABLE IF NOT EXISTS repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    repairman_id BIGINT,
    building VARCHAR(50) NOT NULL,
    dorm_number VARCHAR(20) NOT NULL,
    fault_type_id BIGINT NOT NULL,
    description VARCHAR(500),
    images VARCHAR(1000),
    urgency VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ASSIGN',
    submit_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assign_time TIMESTAMP,
    start_time TIMESTAMP,
    complete_time TIMESTAMP,
    remark VARCHAR(500),
    cancel_reason VARCHAR(500),
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (repairman_id) REFERENCES user(id),
    FOREIGN KEY (fault_type_id) REFERENCES fault_type(id)
);

-- 评价表
CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES repair_order(id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_repair_order_student_id ON repair_order(student_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_repairman_id ON repair_order(repairman_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_status ON repair_order(status);