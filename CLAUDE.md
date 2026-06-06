# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

教师智能教学系统 - 支持考试管理、智能评分、班级画像、PPT生成等功能。

## 技术栈

**后端**: Spring Boot 3.2.0 + Java 17 + MyBatis-Plus 3.5.5 + MySQL 8.x + JWT + Redis(可选)
**前端**: Vue 3 + Vite 5 + Element Plus + Pinia + ECharts + TypeScript

## 架构概览

### 后端模块 (`backend/src/main/java/com/edu/`)

| 模块 | 职责 |
|------|------|
| `common` | 公共层：配置、实体基类、异常、拦截器、Security、工具类 |
| `user` | 用户管理：登录/注册、角色权限 |
| `exam` | 考试管理：题目、试卷、考试安排 |
| `grading` | 智能评分：自动批改、成绩分析 |
| `tenant` | 租户管理：多租户隔离 |
| `ai` | AI服务：Claude API集成、PPT生成 |
| `ppt` | PPT管理：模板、生成记录 |

每层结构：`controller` → `service` → `mapper` → `entity`，DTO 独立包。

### 前端结构 (`frontend/src/`)

| 目录 | 职责 |
|------|------|
| `views/` | 页面组件 |
| `api/` | 后端接口调用（axios封装） |
| `router/` | Vue Router 路由配置 |
| `store/` | Pinia 状态管理 |
| `utils/` | 工具函数 |

### 数据层

- ORM: MyBatis-Plus，XML映射文件在 `backend/src/main/resources/mapper/`
- 逻辑删除: `deleted` 字段（0=未删，1=已删）
- 多租户: TenantInterceptor 拦截器实现租户隔离
- 初始化脚本: `backend/src/main/resources/db/schema.sql`

## 常用命令

### 后端

```bash
cd backend

# 启动开发（需先设置环境变量 DB_PASSWORD）
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行测试
mvn test

# 单模块测试
mvn test -pl backend -Dtest=ClassProfileServiceTest
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

# Mock数据
mysql -u root -p edu_platform < backend/src/main/resources/db/mock-data.sql
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL密码 | 空 |
| `JWT_SECRET` | JWT签名密钥 | 内置默认值 |
| `ANTHROPIC_API_KEY` | Claude API密钥 | 空 |

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

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 开发规范

SDLC 五阶段流程（P1需求→P2编码→P3测试→P4审查→P5交付）详见 `.claude/rules/`。

当前任务状态: Read `.claude/project-state.md` 获取 `current_phase`。

> 可用命令：`/phase`、`/status`、`/checkpoint`、`/review`、`/archive`

## 启动脚本

| 脚本 | 用途 |
|------|------|
| `start-backend.bat` | 启动后端（MySQL版） |
| `start-backend-h2.bat` | 启动后端（H2内存数据库） |
| `start-frontend.bat` | 启动前端开发服务器 |

## gstack

使用 gstack 的 `/browse` 技能进行所有网页浏览。禁止使用 `mcp__claude-in-chrome__*` 工具。

**可用技能：**
- 产品规划：/office-hours, /plan-ceo-review, /plan-eng-review, /plan-design-review, /plan-devex-review
- 设计：/design-consultation, /design-shotgun, /design-html, /design-review
- 开发：/review, /ship, /land-and-deploy, /autoplan, /investigate
- 测试：/qa, /qa-only, /benchmark, /canary
- 文档：/document-release, /document-generate
- 浏览器：/browse, /open-gstack-browser, /setup-browser-cookies
- 安全：/cso, /careful, /guard
- 其他：/retro, /learn, /gstack-upgrade, /freeze, /unfreeze
- 多模型：/codex, /pair-agent
- 知识库：/setup-gbrain, /sync-gbrain

## Skill routing

当收到以下关键词时，自动使用对应的 gstack 技能：
- "浏览/打开网页/截图" → /browse
- "审查代码/review" → /review
- "测试/QA" → /qa
- "部署/ship" → /ship
- "规划新功能" → /office-hours → /autoplan
- "设计UI/界面" → /design-shotgun → /design-html
