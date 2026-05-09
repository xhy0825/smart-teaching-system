@echo off
echo === 教师智能教学系统启动脚本 (H2内存数据库版) ===
echo.
echo 使用H2内存数据库，无需安装MySQL
echo 数据将在程序关闭后清空（仅用于测试）
echo.

REM 设置便携版 Java 17 和 Maven 路径
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set M2_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%

echo [环境检查]
echo Java版本:
java -version
echo.
echo Maven版本:
mvn -version
echo.

echo [编译后端]
cd backend
call mvn clean compile -DskipTests -q
if %errorlevel% neq 0 (
    echo 编译失败
    pause
    exit /b 1
)
echo 编译成功
echo.

echo [启动后端 - H2模式]
echo 后端地址: http://localhost:8080
echo H2控制台: http://localhost:8080/h2-console
echo JDBC URL: jdbc:h2:mem:edu_platform
echo 用户名: sa, 密码: 空
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=h2

pause