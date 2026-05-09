@echo off
echo === 教师智能教学系统 - 环境安装脚本 ===
echo.
echo 请在管理员权限的 PowerShell 或 CMD 中运行此脚本
echo.

echo [1] 安装 Java 17...
winget install EclipseAdoptium.Temurin.17.JDK --accept-source-agreements --accept-package-agreements

echo.
echo [2] 配置 Java 环境变量...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [3] 验证 Java 安装...
java -version

echo.
echo [4] 安装 Maven (手动下载)...
echo 请手动下载 Maven: https://maven.apache.org/download.cgi
echo 或者使用 scoop: scoop install maven

echo.
echo [5] 安装 MySQL...
winget install Oracle.MySQL --accept-source-agreements --accept-package-agreements

echo.
echo === 安装完成 ===
echo.
echo 后续步骤:
echo 1. 创建数据库: CREATE DATABASE edu_platform;
echo 2. 执行脚本: backend\src\main\resources\db\schema.sql
echo 3. 启动后端: start-backend.bat
echo.
pause