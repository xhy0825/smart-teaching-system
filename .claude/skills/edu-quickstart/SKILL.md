---
name: edu-quickstart
description: Use when starting or restarting the edu project for development - covers database init, backend, and frontend startup with process detection
---

# edu平台快速启动

## Overview

快速启动/重启 edu 教学平台前后端，支持进程检测自动重启。

## When to Use

- 首次启动项目需要初始化数据库
- 日常开发快速启动前后端
- 重启服务（自动停止旧进程）

**环境说明：**
- `JAVA_HOME` 和 `MAVEN_HOME` 已配置到系统环境变量
- `java`、`mvn`、`node` 命令可直接使用

**不要用于：**
- 生产环境部署（需额外配置）

## 快速启动

### Windows

```bash
# 启动/重启（自动检测并停止旧进程）
.claude/skills/edu-quickstart/edu-quickstart.bat restart

# 仅启动（不检查旧进程）
.claude/skills/edu-quickstart/edu-quickstart.bat
```

### Linux / macOS

```bash
# 添加执行权限（首次）
chmod +x .claude/skills/edu-quickstart/edu-quickstart.sh

# 启动/重启（自动检测并停止旧进程）
.claude/skills/edu-quickstart/edu-quickstart.sh restart

# 仅启动（不检查旧进程）
.claude/skills/edu-quickstart/edu-quickstart.sh
```

## 端口固定配置

| 服务 | 端口 | 配置文件 |
|------|------|----------|
| 后端 | 8080 | `backend/src/main/resources/application.yml` (server.port) |
| 前端 | 3000 | `frontend/vite.config.ts` (server.port + strictPort: true) |

**端口固定说明：**
- 后端：已在 application.yml 中固定为 8080
- 前端：已在 vite.config.ts 中固定为 3000，并启用 `strictPort: true`（端口被占用时报错而不是切换）
- 使用 `restart` 参数可自动停止旧进程并重启

## 脚本功能

1. **进程检测（restart 模式）**：自动检测端口 8080（后端）和 3000（前端）的占用进程并停止
2. **环境检查**：快速检查 Java、Maven、Node.js 是否可用
3. **数据库初始化**：首次运行时输入 MySQL 密码，自动创建数据库并导入数据
4. **后端启动**：自动编译（如需要）并在新窗口启动（端口 8080）
5. **前端启动**：自动安装依赖（如需要）并在新窗口启动（端口 3000）

## 访问地址

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080/api
- 默认账号：`admin` / `admin123`

## 配置说明

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL 密码 | 运行时输入 |
| `JWT_SECRET` | JWT 密钥 | 已配置在 application.yml |
| `CLAUDE_API_KEY` | Claude API密钥 | 可选 |

## 手动重启

### 停止服务

**Windows：**
```bash
# 停止后端（端口 8080）
for /f "tokens=5" %a in ('netstat -ano ^| findstr ":8080"') do taskkill /f /pid %a

# 停止前端（端口 3000）
for /f "tokens=5" %a in ('netstat -ano ^| findstr ":3000"') do taskkill /f /pid %a
```

**Linux/Mac：**
```bash
# 停止后端
kill -9 $(lsof -ti tcp:8080)

# 停止前端
kill -9 $(lsof -ti tcp:3000)
```

### 启动服务

```bash
# 后端
cd backend && mvn spring-boot:run

# 前端（新终端）
cd frontend && npm run dev
```

## 常见问题

**端口被占用**
```bash
# 查看占用端口的进程
netstat -ano | findstr ":8080"  # Windows
lsof -i :8080                    # Linux/Mac

# 使用 restart 参数自动重启
.claude/skills/edu-quickstart/edu-quickstart.bat restart
```

**数据库连接错误**
- 确认 MySQL 正在运行
- 确认 edu_platform 数据库已创建
- 首次启动需输入 MySQL 密码初始化数据库

**前端无法连接后端**
- 确认后端已启动：http://localhost:8080/api
- 检查前端 API 配置（frontend 中的 axios baseURL）
