@echo off
echo === 教师智能教学系统启动脚本 (便携版) ===
echo.

REM 设置便携版 Java 17 和 Maven 路径
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set M2_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%

echo [环境检查]
java -version
echo.
mvn -version
echo.

echo [启动后端]
echo 请确保 MySQL 数据库已启动并创建了 edu_platform 数据库
echo 数据库脚本: backend\src\main\resources\db\schema.sql
echo.

cd backend
call mvn spring-boot:run -Dspring-boot.run.profiles=simple

pause