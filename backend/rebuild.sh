#!/bin/bash
# 快速重建并重启后端

set -e

echo "=== 停止旧进程 ==="
taskkill //f //im java.exe 2>/dev/null || true
sleep 2

echo "=== 重新构建 ==="
cd D:/JavaWork/edu/backend
rm -rf target
/c/Users/46018/tools/apache-maven-3.9.9/bin/mvn clean package -DskipTests

echo "=== 启动后端 ==="
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.19.10-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
java -jar target/edu-platform-1.0.0-SNAPSHOT.jar > /tmp/edu-backend.log 2>&1 &
sleep 8

echo "=== 验证启动 ==="
grep "Started EduApplication" /tmp/edu-backend.log && echo "✅ 后端启动成功" || echo "❌ 启动失败，查看日志: tail -f /tmp/edu-backend.log"
