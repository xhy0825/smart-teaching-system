@echo off
echo === 教师智能教学系统启动脚本 ===
echo.

echo [1] 检查环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java，请安装Java 17+
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请安装Maven 3.8+
    pause
    exit /b 1
)

echo Java和Maven已安装
echo.

echo [2] 编译后端...
cd backend
call mvn clean compile -DskipTests -q
if %errorlevel% neq 0 (
    echo 错误: 后端编译失败
    pause
    exit /b 1
)
echo 后端编译成功
echo.

echo [3] 启动后端 (需要MySQL数据库运行)...
echo 请确保已创建edu_platform数据库
echo 数据库脚本: backend/src/main/resources/db/schema.sql
echo.
echo 启动中... (按Ctrl+C停止)
call mvn spring-boot:run
pause