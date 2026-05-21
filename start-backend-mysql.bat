@echo off
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set MAVEN_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d D:\JavaWork\edu\backend

echo Starting backend with MySQL...
call mvn spring-boot:run -q
