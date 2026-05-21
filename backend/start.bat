@echo off
chcp 65001 > nul
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set MAVEN_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d D:\JavaWork\edu\backend

echo Starting Teacher AI Education Platform...
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo Login: admin / admin123
echo.

%JAVA_HOME%\bin\java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp "target/classes;target/lib/*" com.edu.EduApplication