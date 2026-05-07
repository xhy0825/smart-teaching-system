@echo off
echo === 前端启动脚本 ===
echo.

cd frontend

echo [1] 检查Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js，请安装Node.js 18+
    pause
    exit /b 1
)
echo Node.js已安装
echo.

echo [2] 检查依赖...
if not exist "node_modules" (
    echo 正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo 错误: npm install失败
        pause
        exit /b 1
    )
)
echo.

echo [3] 启动前端...
echo 访问地址: http://localhost:3000
echo API代理: http://localhost:8080
echo.
call npm run dev
pause