@echo off
echo === 启动后端服务 (H2内存数据库) ===
echo.
echo 使用 H2 内存数据库，无需 MySQL
echo.

REM 设置环境变量
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set M2_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%

echo [1] 检查环境
java -version
echo.

echo [2] 编译后端
cd backend
call mvn clean compile -DskipTests -q
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo 编译成功
echo.

echo [3] 启动后端服务
echo 后端地址: http://localhost:8080
echo H2控制台: http://localhost:8080/h2-console
echo JDBC URL: jdbc:h2:mem:edu_platform
echo 用户名: sa, 密码: 空
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=h2

pause