@echo off
echo === 停止旧进程 ===
taskkill /f /im java.exe 2>nul
timeout /t 2 /nobreak >nul

echo === 重新构建 ===
cd /d D:\JavaWork\edu\backend
rm -rf target
call \c\Users\46018\tools\apache-maven-3.9.9\bin\mvn clean package -DskipTests
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)

echo === 启动后端 ===
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
start /b java -jar target\edu-platform-1.0.0-SNAPSHOT.jar > \tmp\edu-backend.log 2>&1
timeout /t 8 /nobreak >nul

echo === 验证启动 ===
findstr /c:"Started EduApplication" \tmp\edu-backend.log >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端启动成功
) else (
    echo ❌ 启动失败，查看日志: type \tmp\edu-backend.log
)
