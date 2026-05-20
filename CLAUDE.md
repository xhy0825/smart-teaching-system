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
| `views/` | 页面组件（Login、ClassManage、ClassProfile、Grading、Question、QuestionBank、ScoreAnalysis、StudentProfile） |
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
| `CLAUDE_API_KEY` | Claude API密钥 | 空 |

## 开发规范

SDLC 五阶段流程（P1需求→P2编码→P3测试→P4审查→P5交付）详见 `.claude/rules/`。

当前任务状态: Read `.claude/project-state.md` 获取 `current_phase`。

> 可用命令：`/phase`、`/status`、`/checkpoint`、`/review`、`/archive`
