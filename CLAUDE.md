# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

教师智能教学系统 - 支持考试管理、智能评分、班级画像、PPT生成等功能。

## 技术栈

**后端**: Spring Boot 3.2.0 + Java 17 + MyBatis-Plus 3.5.5 + MySQL 8.x + JWT
**前端**: Vue 3 + Vite 5 + Element Plus + Pinia + ECharts + TypeScript

## 架构概览

### 后端模块 (`backend/src/main/java/com/edu/`)

| 模块 | 职责 |
|------|------|
| `common` | 公共层：配置、实体基类、异常、拦截器、Security |
| `user` | 用户管理：登录/注册、角色权限、班级管理 |
| `exam` | 考试管理：题目、试卷、考试安排 |
| `grading` | 智能评分：自动批改、成绩分析 |
| `tenant` | 租户管理：多租户隔离 |
| `ai` | AI服务：LiteLLM Proxy集成、PPT生成 |
| `ppt` | PPT管理：模板、生成记录 |

**分层结构**: `controller` → `service` → `mapper` → `entity`，DTO 独立包。

**关键配置**: 
- 端口: `8080`
- 数据库: `edu_platform`（MySQL 8.x）
- 认证: JWT（24小时过期）
- ORM: MyBatis-Plus，XML映射在 `resources/mapper/`
- 逻辑删除: `deleted` 字段（0=未删，1=已删）

### 前端结构 (`frontend/src/`)

| 目录 | 职责 |
|------|------|
| `views/` | 页面组件（Login、Dashboard、Exam*、Grading等） |
| `api/` | 后端接口调用（axios封装） |
| `router/` | Vue Router 路由配置 |
| `store/` | Pinia 状态管理 |
| `utils/` | 工具函数 |

**关键配置**:
- 开发端口: `3000`
- 代理: `/api` → `http://localhost:8080`（见 `vite.config.ts`）
- 组件库: Element Plus

## 常用命令

### 后端

```bash
cd backend

# 启动开发（需设置环境变量 DB_PASSWORD）
export DB_PASSWORD=your_password  # Linux/Mac
set DB_PASSWORD=your_password     # Windows
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassProfileServiceTest

# 运行单个测试方法
mvn test -Dtest=ClassProfileServiceTest#methodName
```

### 前端

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（http://localhost:3000）
npm run dev

# 构建
npm run build

# 预览构建结果
npm run preview
```

### 数据库

```bash
# 初始化数据库
mysql -u root -p edu_platform < backend/src/main/resources/db/schema.sql

# 初始化数据
mysql -u root -p edu_platform < backend/src/main/resources/db/init-data.sql

# Mock数据（可选）
mysql -u root -p edu_platform < backend/src/main/resources/db/mock-data.sql
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL密码 | `edu2026` |
| `JWT_SECRET` | JWT签名密钥 | 内置默认值 |
| `CLAUDE_API_KEY` | Claude API密钥 | 空（使用LiteLLM Proxy） |

## 配置说明

### 后端配置

主要配置文件: `backend/src/main/resources/application.yml`

- 数据库配置: `spring.datasource`
- JWT配置: `jwt.secret`（过期时间：`jwt.expiration`，默认24小时）
- AI配置: `ai.litellm.*`（使用 LiteLLM Proxy，默认 `http://localhost:8000/v1`）
- Redis: 默认禁用（可选功能）

### 前端配置

主要配置文件: `frontend/vite.config.ts`

- API代理: `/api` → `http://localhost:8080`
- 构建优化: 代码分割、`console.log` 移除

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 开发规范

### SDLC 流程

项目使用五阶段 SDLC 流程（P1→P2→P3→P4→P5），详细规范见 `.claude/rules/`：

- **P1**: 需求分析 + 设计（需求澄清 → 技术调研 → PRD → 架构 → 原型）
- **P2**: 编码实现（按 PRD 执行，可并行）
- **P3**: 测试验证（单元测试 + UI 测试）
- **P4**: 综合审查（代码/测试/集成/PRD追溯）
- **P5**: 部署交付（git + 文档）

当前阶段: 读取 `.claude/project-state.md` 获取 `current_phase`。

**可用命令**: `/phase`、`/status`、`/checkpoint`、`/review`、`/archive`

### Git 工作流

- 分支命名: `feature/*`、`fix/*`、`refactor/*`、`docs/*`
- 禁止: `push --force`（main）、`reset --hard`
- 详见: `.claude/rules/04-git-workflow.md`

### UI/UX 规范

- 强制使用现代组件库（Element Plus/Ant Design 5+）
- 设计系统: 使用 `ui-ux-pro-max` skill
- 响应式: 375px/768px/1440px 断点
- 可访问性: WCAG 2.1 AA
- 详见: `.claude/rules/10-ui-ux-standards.md`

## 快速启动

### 方式1: 使用启动脚本（Windows）

```bash
# 启动后端（MySQL版）
start-backend.bat

# 启动后端（H2内存数据库，无需MySQL）
start-backend-h2.bat

# 启动前端
start-frontend.bat
```

### 方式2: 手动启动

```bash
# 终端1: 启动后端
cd backend
set DB_PASSWORD=your_password
mvn spring-boot:run

# 终端2: 启动前端
cd frontend
npm run dev
```

访问: http://localhost:3000

## 开发技巧

1. **后端热重载**: 使用 `spring-boot-devtools`（已配置）
2. **前端热重载**: Vite 默认支持
3. **数据库切换**: 修改 `application.yml` 中的 `spring.datasource.url`
4. **JWT 调试**: 设置 `logging.level.com.edu: debug`
5. **MyBatis-Plus 日志**: 已启用 `StdOutImpl`（见 `application.yml`）

## gstack 集成

使用 gstack 的 `/browse` 技能进行网页浏览。禁止使用 `mcp__claude-in-chrome__*` 工具。

**常用技能触发词**:
- "浏览/打开网页/截图" → `/browse`
- "审查代码/review" → `/review`
- "测试/QA" → `/qa`
- "部署/ship" → `/ship`
- "规划新功能" → `/office-hours` → `/autoplan`
- "设计UI/界面" → `/design-shotgun` → `/design-html`

完整技能列表: 输入 `/` 查看所有可用技能。
