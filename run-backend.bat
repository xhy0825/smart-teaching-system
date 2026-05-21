@echo off
cd /d D:\JavaWork\edu\backend
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set MAVEN_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
mvn spring-boot:run > D:\JavaWork\edu\backend-output.log 2>&1
