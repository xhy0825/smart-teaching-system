# 教师智能教学系统启动指南

## 环境要求

- **后端**: Java 17+, Maven 3.8+, MySQL 8.x, Redis 7.x (可选)
- **前端**: Node.js 18+, npm 9+

## 1. 数据库配置

### 创建MySQL数据库

```sql
CREATE DATABASE edu_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 执行数据库脚本

```bash
mysql -u root -p edu_platform < backend/src/main/resources/db/schema.sql
```

### 配置数据库连接

修改 `backend/src/main/resources/application.yml` 或设置环境变量:

```bash
# Windows CMD
set DB_PASSWORD=your_password

# Windows PowerShell
$env:DB_PASSWORD="your_password"

# Linux/Mac
export DB_PASSWORD=your_password
```

## 2. 启动后端

### 方式一: 使用Maven

```bash
cd backend
mvn spring-boot:run
```

### 方式二: 打包后运行

```bash
cd backend
mvn clean package -DskipTests
java -jar target/edu-platform-1.0.0-SNAPSHOT.jar
```

## 3. 启动前端

### 安装依赖

```bash
cd frontend
npm install
```

### 开发模式启动

```bash
npm run dev
```

访问: http://localhost:3000

## 4. 访问系统

- **前端**: http://localhost:3000
- **后端API**: http://localhost:8080/api

## 常见问题

### Q: Maven命令找不到?
确保已安装Maven并添加到PATH环境变量。

### Q: 数据库连接失败?
检查MySQL是否运行，用户名密码是否正确，数据库是否已创建。

### Q: 前端启动失败?
确保已执行 `npm install` 安装依赖。

### Q: Redis连接失败?
Redis是可选的，可以暂时注释掉Redis相关配置，或安装Redis服务。