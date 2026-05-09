@echo off
set JAVA_HOME=C:\Users\46018\tools\jdk-17.0.13+11
set MAVEN_HOME=C:\Users\46018\tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d D:\JavaWork\edu\backend

echo Building classpath...
call %MAVEN_HOME%\bin\mvn.cmd dependency:copy-dependencies -DoutputDirectory=target/lib -o -Dmaven.test.skip=true

echo Starting application with H2...
%JAVA_HOME%\bin\java -cp "target/classes;target/lib/*" com.edu.EduApplication --spring.profiles.active=h2