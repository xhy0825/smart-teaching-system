# 教师智能教学系统 - 环境安装指南

## 快速安装（推荐）

### 1. 以管理员身份运行 Chocolatey 安装

打开 **PowerShell** 或 **CMD**，右键选择 "以管理员身份运行"，执行：

```powershell
# 安装 Java 17
choco install temurin17 -y

# 安装 Maven
choco install maven -y

# 安装 MySQL
choco install mysql -y
```

安装完成后重启终端。

---

### 2. 配置 MySQL 数据库

```powershell
# 启动 MySQL 服务
net start mysql

# 登录 MySQL（初始密码可能为空或需要查看安装日志）
mysql -u root -p

# 创建数据库
CREATE DATABASE edu_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE edu_platform;

# 执行初始化脚本
SOURCE D:/JavaWork/edu/backend/src/main/resources/db/schema.sql;
SOURCE D:/JavaWork/edu/backend/src/main/resources/db/init-data.sql;

# 退出
EXIT;
```

---

### 3. 配置数据库连接

编辑 `backend/src/main/resources/application-simple.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/edu_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 你的MySQL密码  # 修改这里
```

---

### 4. 启动系统

```powershell
# 启动后端（在管理员终端）
cd D:\JavaWork\edu
start-backend.bat

# 或手动启动
cd backend
mvn spring-boot:run

# 启动前端（新终端窗口）
start-frontend.bat
```

---

## 手动下载安装

如果 Chocolatey 无法使用，手动下载安装：

### Java 17
- 下载：https://adoptium.net/temurin/releases/?version=17
- 选择 Windows x64，下载 .msi 或 .zip
- 安装后设置环境变量 `JAVA_HOME`

### Maven 3.9
- 下载：https://maven.apache.org/download.cgi
- 选择 Binary zip archive (apache-maven-3.9.x-bin.zip)
- 解压到 `C:\Program Files\Apache\maven`
- 添加到 PATH：`C:\Program Files\Apache\maven\bin`

### MySQL 8
- 下载：https://dev.mysql.com/downloads/mysql/
- 选择 Windows (x86, 64-bit), ZIP Archive 或 MSI Installer
- 安装时设置 root 密码
- 创建 edu_platform 数据库并执行初始化脚本

---

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Vue 开发服务器 |
| 后端 | 8080 | Spring Boot API |
| MySQL | 3306 | 数据库 |

---

## 默认登录账号

- 用户名：`admin`
- 密码：`admin123`

---

## 验证安装

```powershell
# 检查 Java
java -version
# 应输出：openjdk version "17.x.x" 或 java version "17.x.x"

# 检查 Maven
mvn -version
# 应输出：Apache Maven 3.9.x

# 检查 MySQL
mysql -u root -p -e "SELECT VERSION();"
# 应输出 MySQL 版本
```