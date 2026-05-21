#!/bin/bash

# ===================================
# edu平台快速启动/重启脚本
# 用法: ./edu-quickstart.sh [restart]
# ===================================

DB_NAME=edu_platform
DB_USER=root
BACKEND_DIR=backend
FRONTEND_DIR=frontend
API_PORT=8080
WEB_PORT=3000
ACTION=${1:-start}

echo "=================================="
echo "  edu平台快速启动"
echo "=================================="
echo

# ===================================
# 1. 停止旧进程（如果是重启）
# ===================================
if [ "$ACTION" = "restart" ]; then
    echo "[1/3] 停止旧进程..."

    # 停止后端（端口 8080）
    PIDS=$(lsof -ti tcp:$API_PORT 2>/dev/null)
    if [ -n "$PIDS" ]; then
        echo "  停止后端进程: $PIDS"
        kill -9 $PIDS 2>/dev/null
    fi

    # 停止前端（端口 3000）
    PIDS=$(lsof -ti tcp:$WEB_PORT 2>/dev/null)
    if [ -n "$PIDS" ]; then
        echo "  停止前端进程: $PIDS"
        kill -9 $PIDS 2>/dev/null
    fi

    # 备用方案：通过进程名停止
    pkill -9 -f "spring-boot:run" 2>/dev/null
    pkill -9 -f "vite" 2>/dev/null

    sleep 2
    echo "  旧进程已停止"
    echo
fi

# ===================================
# 2. 快速环境检查（仅关键项）
# ===================================
echo "[2/3] 检查环境..."

command -v java >/dev/null 2>&1 || { echo "[错误] 未找到Java"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "[错误] 未找到Maven"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "[错误] 未找到Node.js"; exit 1; }
echo "  环境检查通过"
echo

# ===================================
# 3. 数据库初始化（仅首次）
# ===================================
echo "[3/3] 检查数据库..."

read -sp "MySQL密码（直接回车跳过）: " DB_PASSWORD
echo

if [ -n "$DB_PASSWORD" ]; then
    if ! mysql -u "$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null; then
        echo "  初始化数据库..."
        mysql -u "$DB_USER" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || { echo "[错误] 创建数据库失败"; exit 1; }
        mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$BACKEND_DIR/src/main/resources/db/schema.sql" 2>/dev/null
        [ -f "$BACKEND_DIR/src/main/resources/db/mock-data.sql" ] && mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$BACKEND_DIR/src/main/resources/db/mock-data.sql" 2>/dev/null
        echo "  数据库初始化完成"
    else
        echo "  数据库已存在，跳过初始化"
    fi
else
    echo "  跳过数据库检查"
fi
echo

# ===================================
# 4. 启动后端
# ===================================
echo "启动后端..."

cd "$BACKEND_DIR" || exit 1

[ ! -d "target/classes" ] && { echo "  编译中..." && mvn compile -DskipTests -q || { echo "[错误] 编译失败"; exit 1; }; }

export DB_PASSWORD
mvn spring-boot:run &
BACKEND_PID=$!
echo "  后端启动中 (PID: $BACKEND_PID) -> http://localhost:$API_PORT"
cd ..
echo

# ===================================
# 5. 启动前端
# ===================================
echo "启动前端..."

cd "$FRONTEND_DIR" || exit 1

[ ! -d "node_modules" ] && { echo "  安装依赖..." && npm install || { echo "[错误] 依赖安装失败"; kill $BACKEND_PID 2>/dev/null; exit 1; }; }

echo
echo "=================================="
echo "  启动完成！"
echo "=================================="
echo "  后端: http://localhost:$API_PORT/api"
echo "  前端: http://localhost:$WEB_PORT"
echo "  账号: admin / admin123"
echo
echo "  按Ctrl+C停止所有服务"
echo "=================================="
echo

npm run dev

# 清理：停止后端进程
kill $BACKEND_PID 2>/dev/null
