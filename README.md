# RepairingSystem

宿舍维修管理系统

## 功能特性

- 三端分立：学生端、修理工端、管理员端独立运行
- 实时通信：WebSocket 实时推送工单状态更新
- 智能分配：基于多维度评分的工单智能分配算法
- 文件上传：支持报修图片和维修结果图片上传
- 批量管理：支持批量导入学生和修理工
- 智能助手：集成 DeepSeek AI 对话助手

## 技术栈

### 后端
- Spring Boot 2.7.x
- Spring Security + JWT
- MyBatis Plus
- WebSocket (Stomp + SockJS)
- MySQL

### 前端
- Vue 3
- Pinia
- Element Plus
- Axios
- WebSocket Client

## 快速开始

### 前置要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE repair_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据表（可选，系统会自动创建）

### 环境变量配置

创建 `.env` 文件（不要提交到 Git）：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=repair_db
DB_USERNAME=root
DB_PASSWORD=your-database-password

# JWT 配置
JWT_SECRET=your-jwt-secret-key-here

# DeepSeek API 配置
DEEPSEEK_API_KEY=your-deepseek-api-key

# 文件上传路径
UPLOAD_PATH=uploads
```

### 后端启动

```bash
cd backend
mvn clean package
java -jar target/repair-1.0.0.jar
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问地址：http://localhost:5173

## 默认账号

| 角色 | 用户名       | 密码 |
|------|-----------|------|
| 管理员 | admin     | 123456 |
| 学生 | 231010101 | 123456 |
| 修理工 | 0001      | 123456 |

## 部署说明

详细的部署配置请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

## 安全说明

- 不要将 `.env` 文件提交到 Git
- 生产环境请修改默认密码和密钥
- 使用 HTTPS 保护数据传输
- 定期备份数据库

## 许可证

MIT License