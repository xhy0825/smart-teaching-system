@echo off
REM ===================================
REM edu平台快速启动/重启脚本
REM 用法: edu-quickstart.bat [restart]
REM ===================================

setlocal EnableDelayedExpansion

set DB_NAME=edu_platform
set DB_USER=root
set BACKEND_DIR=backend
set FRONTEND_DIR=frontend
set API_PORT=8080
set WEB_PORT=3000
set ACTION=%1

echo ===================================
echo  edu平台快速启动
echo ===================================
echo.

REM ===================================
REM 1. 停止旧进程（如果是重启）
REM ===================================
if "%ACTION%"=="restart" (
    echo [1/3] 停止旧进程...
    REM 停止后端（端口 8080）
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%API_PORT%" ^| findstr /v "findstr"') do (
        echo  停止后端进程 (PID: %%a)...
        taskkill /f /pid %%a >nul 2>&1
    )
    REM 停止前端（端口 3000）
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%WEB_PORT%" ^| findstr /v "findstr"') do (
        echo  停止前端进程 (PID: %%a)...
        taskkill /f /pid %%a >nul 2>&1
    )
    REM 备用方案：通过进程名停止
    taskkill /f /im java.exe >nul 2>&1
    taskkill /f /im node.exe >nul 2>&1
    echo  旧进程已停止
    echo.
)

REM ===================================
REM 2. 快速环境检查（仅关键项）
REM ===================================
echo [2/3] 检查环境...

where java >nul 2>&1 || (echo [错误] 未找到Java & pause & exit /b 1)
where mvn >nul 2>&1 || (echo [错误] 未找到Maven & pause & exit /b 1)
where node >nul 2>&1 || (echo [错误] 未找到Node.js & pause & exit /b 1)
echo  环境检查通过
echo.

REM ===================================
REM 3. 数据库初始化（仅首次）
REM ===================================
echo [3/3] 检查数据库...
set DB_PASSWORD=
set /p DB_PASSWORD="MySQL密码（直接回车跳过）: "

if not "%DB_PASSWORD%"=="" (
    mysql -u %DB_USER% -p%DB_PASSWORD% -e "USE %DB_NAME%;" 2>nul || (
        echo  初始化数据库...
        mysql -u %DB_USER% -p%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < %BACKEND_DIR%/src/main/resources/db/schema.sql 2>nul
        if exist %BACKEND_DIR%/src/main/resources/db/mock-data.sql (
            mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < %BACKEND_DIR%/src/main/resources/db/mock-data.sql 2>nul
        )
        echo  数据库初始化完成
    )
) else (
    echo  跳过数据库检查
)
echo.

REM ===================================
REM 4. 启动后端
REM ===================================
echo 启动后端...
cd %BACKEND_DIR%
if not exist "target\classes" (
    echo  编译中...
    call mvn compile -DskipTests -q || (echo [错误] 编译失败 & pause & exit /b 1)
)
start "edu-backend" cmd /k "set DB_PASSWORD=%DB_PASSWORD% && mvn spring-boot:run"
cd ..
echo  后端启动中 (http://localhost:%API_PORT%)
echo.

REM ===================================
REM 5. 启动前端
REM ===================================
echo 启动前端...
cd %FRONTEND_DIR%
if not exist "node_modules" (
    echo  安装依赖...
    call npm install || (echo [错误] 依赖安装失败 & pause & exit /b 1)
)
start "edu-frontend" cmd /k "npm run dev"
cd ..
echo  前端启动中 (http://localhost:%WEB_PORT%)
echo.

REM ===================================
REM 完成
REM ===================================
echo ===================================
echo  启动完成！
echo ===================================
echo  后端: http://localhost:%API_PORT%/api
echo  前端: http://localhost:%WEB_PORT%
echo  账号: admin / admin123
echo.
echo  关闭窗口可停止服务
echo ===================================
pause
