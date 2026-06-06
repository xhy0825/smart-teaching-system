# 教师智能教学系统

基于 Spring Boot + Vue 3 的智能教学管理平台，集成 AI 能力，支持考试管理、智能评分、班级画像、PPT生成等功能。

## 功能特性

- **用户管理**: 多角色权限控制（管理员、教师、学生）
- **考试管理**: 题库管理、试卷生成、在线考试
- **智能评分**: AI辅助批改、成绩分析、错题统计
- **班级画像**: 学生成绩分析、知识点掌握度雷达图
- **AI集成**: Claude API 集成、智能出题、拍照批改
- **PPT生成**: 基于模板的快速PPT生成

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0 + Java 17
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.x + Redis (可选)
- **安全**: JWT + Spring Security
- **AI**: Claude API / LiteLLM Proxy

### 前端
- **框架**: Vue 3 + TypeScript
- **构建**: Vite 5
- **UI库**: Element Plus
- **状态管理**: Pinia
- **图表**: ECharts 6

## 快速开始

### 环境要求

- **后端**: Java 17+, Maven 3.8+, MySQL 8.x
- **前端**: Node.js 18+, npm 9+

### 1. 数据库配置

创建数据库并初始化:

```bash
mysql -u root -p -e "CREATE DATABASE edu_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p edu_platform < backend/src/main/resources/db/schema.sql
```

### 2. 配置环境变量

设置数据库连接密码:

```bash
# Windows
set DB_PASSWORD=your_password

# Linux/Mac
export DB_PASSWORD=your_password
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端API: http://localhost:8080/api

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端界面: http://localhost:3000

### 5. 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 项目结构

```
smart-teaching-system/
├── backend/              # Spring Boot 后端
│   └── src/main/java/com/edu/
│       ├── common/       # 公共模块
│       ├── user/         # 用户管理
│       ├── exam/         # 考试管理
│       ├── grading/      # 智能评分
│       ├── tenant/       # 租户管理
│       ├── ai/           # AI服务
│       └── ppt/          # PPT管理
├── frontend/             # Vue 3 前端
│   └── src/
│       ├── views/        # 页面组件
│       ├── api/          # 接口调用
│       ├── router/       # 路由配置
│       ├── store/        # 状态管理
│       └── utils/        # 工具函数
├── start-backend.bat          # 后端启动脚本（MySQL）
├── start-backend-h2.bat      # 后端启动脚本（H2）
└── start-frontend.bat         # 前端启动脚本
```

## 开发指南

### 后端开发

```bash
cd backend

# 编译
mvn clean compile -DskipTests

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests
```

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

## 配置说明

### 后端配置

主要配置文件: `backend/src/main/resources/application.yml`

- 数据库配置: 修改 `spring.datasource`
- JWT配置: 修改 `jwt.secret`
- AI配置: 修改 `ai.*`

### 前端配置

主要配置文件: `frontend/vite.config.ts`

- 代理设置: `server.proxy`
- 构建优化: 代码分割、console移除

## 常见问题

### Q: Maven命令找不到?
确保已安装Maven并添加到PATH环境变量。

### Q: 数据库连接失败?
检查MySQL是否运行，用户名密码是否正确，数据库是否已创建。

### Q: 前端启动失败?
确保已执行 `npm install` 安装依赖。

### Q: Redis连接失败?
Redis是可选的，可以暂时注释掉Redis相关配置，或安装Redis服务。

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/xhy0825/smart-teaching-system
- 问题反馈: https://github.com/xhy0825/smart-teaching-system/issues

---

**注意**: 本项目使用 Claude Code 进行开发，遵循 SDLC 五阶段流程（P1需求→P2编码→P3测试→P4审查→P5交付）。
