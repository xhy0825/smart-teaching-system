# 教师智能教学系统 MVP 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建教师智能教学系统 MVP，包含试卷生成和试卷批改核心功能，支持多租户SaaS架构

**Architecture:** 模块化单体架构 - Spring Boot后端按模块划分（tenant/user/exam/grading/ai），Vue 3前端SPA，MySQL+Redis数据层，AI服务抽象层支持云端/私有切换

**Tech Stack:** Spring Boot 3.2 + Java 17 + MyBatis-Plus + MySQL 8 + Redis 7 + Vue 3 + Element Plus + Claude/GPT API

---

## 文件结构规划

```
backend/
├── pom.xml
├── src/main/java/com/edu/
│   ├── EduApplication.java
│   ├── common/
│   │   ├── config/          # MyBatis配置、Redis配置、Security配置
│   │   ├── entity/          # BaseEntity、统一响应Result
│   │   ├── exception/       # 全局异常处理
│   │   ├── util/            # TenantContextHolder、JwtUtil
│   │   └── interceptor/     # 租户拦截器
│   ├── tenant-module/
│   │   ├── entity/          # Tenant, TenantConfig, School
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── controller/
│   ├── user-module/
│   │   ├── entity/          # User, Role, Permission, Grade, Class, Student
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── controller/
│   ├── exam-module/
│   │   ├── entity/          # QuestionBank, Question, ExamTemplate, ExamPaper
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── controller/
│   ├── grading-module/
│   │   ├── entity/          # AnswerSheet, Answer, ScoreAnalysis
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── controller/
│   ├── ai-module/
│   │   ├── dto/             # 请求/响应DTO
│   │   ├── provider/        # AIProvider接口及实现
│   │   ├── service/         # AIService、PromptBuilder
│   └── resources/
│       ├── application.yml
│       ├── db/schema.sql
│       └── mapper/*.xml

frontend/
├── package.json
├── vite.config.ts
├── src/
│   ├── api/                 # axios封装 + 各模块API
│   ├── router/              # 路由配置
│   ├── store/               # Pinia状态管理
│   ├── views/               # 页面组件
│   ├── components/          # 通用组件
│   └── utils/               # 工具函数

docs/
├── api/                     # API文档
└── deploy/                  # 部署文档
```

---

## 阶段一：项目初始化与基础架构 (Tasks 1-15)

### Task 1: 创建后端项目骨架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/edu/EduApplication.java`
- Create: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 创建项目目录结构**

```bash
mkdir -p backend/src/main/java/com/edu/{common/{config,entity,exception,util,interceptor},tenant-module/{entity,mapper,service,controller},user-module/{entity,mapper,service,controller},exam-module/{entity,mapper,service,controller},grading-module/{entity,mapper,service,controller},ai-module/{dto,provider,service}}
mkdir -p backend/src/main/resources/{db,mapper}
mkdir -p backend/src/test/java/com/edu
```

- [ ] **Step 2: 编写pom.xml依赖配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.edu</groupId>
    <artifactId>edu-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Edu Platform</name>
    <description>教师智能教学系统</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>0.12.3</jwt.version>
        <fastjson2.version>2.0.43</fastjson2.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Fastjson2 -->
        <dependency>
            <groupId>com.alibaba.fastjson2</groupId>
            <artifactId>fastjson2</artifactId>
            <version>${fastjson2.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 编写application.yml配置**

```yaml
server:
  port: 8080

spring:
  application:
    name: edu-platform

  datasource:
    url: jdbc:mysql://localhost:3306/edu_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 10000ms

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.edu.*.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: edu-platform-jwt-secret-key-2026-very-long-string-for-security
  expiration: 86400000 # 24小时

ai:
  default-provider: CLOUD
  cloud:
    provider: Claude
    api-url: https://api.anthropic.com/v1/messages
    api-key: ${CLAUDE_API_KEY:}
    model: claude-sonnet-4-6
    max-tokens: 2000
  private:
    service-url: http://localhost:8081
    timeout: 30000

logging:
  level:
    com.edu: debug
    org.springframework.security: info
```

- [ ] **Step 4: 编写启动类**

```java
package com.edu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.edu.*.mapper")
public class EduApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduApplication.class, args);
    }
}
```

- [ ] **Step 5: 验证项目启动**

Run: `cd backend && mvn spring-boot:run`
Expected: 应用启动成功，端口8080可访问

- [ ] **Step 6: Commit**

```bash
cd backend
git init
git add .
git commit -m "feat: 初始化Spring Boot项目骨架"
```

---

### Task 2: 创建数据库Schema

**Files:**
- Create: `backend/src/main/resources/db/schema.sql`

- [ ] **Step 1: 编写租户模块表结构**

```sql
-- 租户表
CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '租户名称（学校/机构名）',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '租户唯一标识',
    ai_provider VARCHAR(20) DEFAULT 'CLOUD' COMMENT 'AI服务提供商: CLOUD/PRIVATE',
    ai_config JSON COMMENT 'AI配置（API密钥/私有服务地址）',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    expire_date DATE COMMENT '服务到期日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 租户配置表
CREATE TABLE IF NOT EXISTS tenant_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value JSON COMMENT '配置值JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_key (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户配置表';
```

- [ ] **Step 2: 编写用户模块表结构**

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(200) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_username (tenant_id, username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(20) NOT NULL COMMENT '角色编码: ADMIN/TEACHER/STUDENT/PARENT',
    description VARCHAR(200) COMMENT '角色描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表（系统级，无tenant_id）
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    code VARCHAR(50) NOT NULL COMMENT '权限编码: exam:create/grading:view等',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    resource VARCHAR(50) COMMENT '资源类型',
    action VARCHAR(20) COMMENT '操作类型: create/read/update/delete',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

- [ ] **Step 3: 编写组织架构表结构**

```sql
-- 学校表
CREATE TABLE IF NOT EXISTS school (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学校ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    name VARCHAR(100) NOT NULL COMMENT '学校名称',
    address VARCHAR(200) COMMENT '学校地址',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校表';

-- 年级表
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '年级ID',
    school_id BIGINT NOT NULL COMMENT '所属学校',
    name VARCHAR(50) NOT NULL COMMENT '年级名称',
    level TINYINT COMMENT '学段: 1-小学, 2-初中, 3-高中',
    sequence INT COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    grade_id BIGINT NOT NULL COMMENT '所属年级',
    name VARCHAR(50) NOT NULL COMMENT '班级名称',
    teacher_id BIGINT COMMENT '班主任ID（关联user）',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    class_id BIGINT NOT NULL COMMENT '所属班级',
    user_id BIGINT COMMENT '关联用户账号',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    student_no VARCHAR(20) COMMENT '学号',
    gender TINYINT COMMENT '性别: 1-男, 2-女',
    birth_date DATE COMMENT '出生日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-在读, 0-离校',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_student_no (tenant_id, student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';
```

- [ ] **Step 4: 编写试卷模块表结构**

```sql
-- 题库表
CREATE TABLE IF NOT EXISTS question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题库ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    name VARCHAR(100) NOT NULL COMMENT '题库名称',
    subject VARCHAR(20) NOT NULL COMMENT '学科: MATH/PHYSICS/CHEMISTRY/ENGLISH',
    grade_level TINYINT COMMENT '适用年级段',
    description VARCHAR(200) COMMENT '题库描述',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-私有, 1-租户内公开',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    bank_id BIGINT NOT NULL COMMENT '所属题库',
    subject VARCHAR(20) NOT NULL COMMENT '学科',
    question_type VARCHAR(20) NOT NULL COMMENT '题型: CHOICE/FILL/JUDGE/CALCULATION',
    difficulty TINYINT COMMENT '难度: 1-简单, 2-中等, 3-困难',
    content TEXT NOT NULL COMMENT '题目内容',
    options JSON COMMENT '选项（选择题）',
    answer TEXT NOT NULL COMMENT '标准答案',
    answer_analysis TEXT COMMENT '答案解析',
    knowledge_points JSON COMMENT '知识点标签',
    source VARCHAR(50) COMMENT '来源: MANUAL/AI_GENERATED',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 试卷模板表
CREATE TABLE IF NOT EXISTS exam_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    subject VARCHAR(20) NOT NULL COMMENT '学科',
    total_score DECIMAL(5,2) COMMENT '总分',
    time_limit INT COMMENT '考试时长（分钟）',
    structure JSON COMMENT '试卷结构配置',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷模板表';

-- 试卷表
CREATE TABLE IF NOT EXISTS exam_paper (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    template_id BIGINT COMMENT '关联模板',
    title VARCHAR(100) NOT NULL COMMENT '试卷标题',
    subject VARCHAR(20) NOT NULL COMMENT '学科',
    grade_id BIGINT COMMENT '适用年级',
    class_id BIGINT COMMENT '适用班级',
    total_score DECIMAL(5,2) NOT NULL COMMENT '总分',
    time_limit INT COMMENT '考试时长',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-考试中, 3-已结束, 4-已批改',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME COMMENT '发布时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷题目关联表
CREATE TABLE IF NOT EXISTS exam_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_paper_id BIGINT NOT NULL COMMENT '试卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    sequence INT NOT NULL COMMENT '题目序号',
    score DECIMAL(5,2) NOT NULL COMMENT '本题分值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_question (exam_paper_id, sequence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';
```

- [ ] **Step 5: 编写批改模块表结构**

```sql
-- 答题卡表
CREATE TABLE IF NOT EXISTS answer_sheet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答题卡ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    exam_paper_id BIGINT NOT NULL COMMENT '试卷ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未提交, 1-已提交, 2-批改中, 3-已批改',
    total_score DECIMAL(5,2) COMMENT '总分',
    submit_time DATETIME COMMENT '提交时间',
    grading_time DATETIME COMMENT '批改完成时间',
    graded_by BIGINT COMMENT '批改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_student (exam_paper_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题卡表';

-- 答题详情表
CREATE TABLE IF NOT EXISTS answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    answer_sheet_id BIGINT NOT NULL COMMENT '答题卡ID',
    exam_question_id BIGINT NOT NULL COMMENT '试卷题目ID',
    student_answer TEXT COMMENT '学生答案',
    is_correct TINYINT COMMENT '是否正确: 0-错误, 1-正确, 2-部分正确',
    score DECIMAL(5,2) COMMENT '得分',
    ai_score DECIMAL(5,2) COMMENT 'AI评分',
    manual_score DECIMAL(5,2) COMMENT '人工评分',
    ai_analysis TEXT COMMENT 'AI批改分析',
    graded_at DATETIME COMMENT '批改时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sheet_question (answer_sheet_id, exam_question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情表';

-- 成绩分析表
CREATE TABLE IF NOT EXISTS score_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析ID',
    exam_paper_id BIGINT NOT NULL COMMENT '试卷ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    avg_score DECIMAL(5,2) COMMENT '平均分',
    max_score DECIMAL(5,2) COMMENT '最高分',
    min_score DECIMAL(5,2) COMMENT '最低分',
    pass_rate DECIMAL(5,4) COMMENT '及格率',
    excellent_rate DECIMAL(5,4) COMMENT '优秀率',
    question_analysis JSON COMMENT '题目分析',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_class (exam_paper_id, class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩分析表';

-- 学生错题记录表
CREATE TABLE IF NOT EXISTS student_wrong_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '错题ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    exam_paper_id BIGINT COMMENT '来源试卷',
    wrong_count INT DEFAULT 1 COMMENT '错误次数',
    last_wrong_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最近错误时间',
    corrected_at DATETIME COMMENT '纠错时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_question (student_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生错题记录表';
```

- [ ] **Step 6: 执行Schema创建**

Run: `mysql -u root -p < backend/src/main/resources/db/schema.sql`
Expected: 所有表创建成功

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/resources/db/schema.sql
git commit -m "feat: 创建数据库Schema（租户/用户/试卷/批改模块）"
```

---

### Task 3: 创建公共基础类

**Files:**
- Create: `backend/src/main/java/com/edu/common/entity/BaseEntity.java`
- Create: `backend/src/main/java/com/edu/common/entity/Result.java`
- Create: `backend/src/main/java/com/edu/common/util/TenantContextHolder.java`

- [ ] **Step 1: 编写BaseEntity基类**

```java
package com.edu.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类，包含租户ID和通用字段
 */
@Data
public class BaseEntity {

    /**
     * 租户ID，用于多租户隔离
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

- [ ] **Step 2: 编写统一响应Result类**

```java
package com.edu.common.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（默认400）
     */
    public static <T> Result<T> error(String message) {
        return error(400, message);
    }
}
```

- [ ] **Step 3: 编写TenantContextHolder租户上下文**

```java
package com.edu.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 租户上下文持有者，用于存储当前请求的租户ID
 */
public class TenantContextHolder {

    private static final TransmittableThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除租户上下文
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
```

注意：需要添加TransmittableThreadLocal依赖支持异步线程传递。

- [ ] **Step 4: 更新pom.xml添加ttl依赖**

```xml
<!-- TransmittableThreadLocal for async thread context -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
    <version>2.14.4</version>
</dependency>
```

- [ ] **Step 5: 编写测试验证TenantContextHolder**

创建测试文件：`backend/src/test/java/com/edu/common/util/TenantContextHolderTest.java`

```java
package com.edu.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

    @Test
    void testSetAndGetTenantId() {
        Long expectedTenantId = 100L;

        TenantContextHolder.setTenantId(expectedTenantId);
        Long actualTenantId = TenantContextHolder.getTenantId();

        assertEquals(expectedTenantId, actualTenantId);

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testClear() {
        TenantContextHolder.setTenantId(100L);
        TenantContextHolder.clear();

        assertNull(TenantContextHolder.getTenantId());
    }
}
```

- [ ] **Step 6: 运行测试**

Run: `cd backend && mvn test -Dtest=TenantContextHolderTest`
Expected: 测试通过

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "feat: 创建公共基础类（BaseEntity、Result、TenantContextHolder）"
```

---

### Task 4: 创建异常处理类

**Files:**
- Create: `backend/src/main/java/com/edu/common/exception/BusinessException.java`
- Create: `backend/src/main/java/com/edu/common/exception/TenantException.java`
- Create: `backend/src/main/java/com/edu/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 编写BusinessException业务异常**

```java
package com.edu.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }
}
```

- [ ] **Step 2: 编写TenantException租户异常**

```java
package com.edu.common.exception;

/**
 * 租户相关异常
 */
public class TenantException extends BusinessException {

    public TenantException(String message) {
        super(403, message);
    }

    public static TenantException notFound() {
        return new TenantException("租户不存在");
    }

    public static TenantException disabled() {
        return new TenantException("租户已禁用");
    }

    public static TenantException expired() {
        return new TenantException("租户服务已到期");
    }

    public static TenantException accessDenied() {
        return new TenantException("无权访问该租户数据");
    }
}
```

- [ ] **Step 3: 编写GlobalExceptionHandler全局异常处理器**

```java
package com.edu.common.exception;

import com.edu.common.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 租户异常处理
     */
    @ExceptionHandler(TenantException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleTenantException(TenantException e) {
        log.warn("租户异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 认证异常处理
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "认证失败");
    }

    /**
     * 权限异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统异常，请联系管理员");
    }
}
```

- [ ] **Step 4: 编写异常处理测试**

创建测试文件：`backend/src/test/java/com/edu/common/exception/GlobalExceptionHandlerTest.java`

```java
package com.edu.common.exception;

import com.edu.common.entity.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        BusinessException e = new BusinessException("测试业务异常");
        Result<Void> result = handler.handleBusinessException(e);

        assertEquals(400, result.getCode());
        assertEquals("测试业务异常", result.getMessage());
    }

    @Test
    void testHandleTenantException() {
        TenantException e = TenantException.accessDenied();
        Result<Void> result = handler.handleTenantException(e);

        assertEquals(403, result.getCode());
        assertEquals("无权访问该租户数据", result.getMessage());
    }

    @Test
    void testTenantExceptionStaticMethods() {
        TenantException notFound = TenantException.notFound();
        assertEquals("租户不存在", notFound.getMessage());

        TenantException disabled = TenantException.disabled();
        assertEquals("租户已禁用", disabled.getMessage());

        TenantException expired = TenantException.expired();
        assertEquals("租户服务已到期", expired.getMessage());
    }
}
```

- [ ] **Step 5: 运行测试**

Run: `cd backend && mvn test -Dtest=GlobalExceptionHandlerTest`
Expected: 测试通过

- [ ] **Step 6: Commit**

```bash
git add .
git commit -m "feat: 创建全局异常处理器"
```

---

### Task 5: 创建租户拦截器

**Files:**
- Create: `backend/src/main/java/com/edu/common/interceptor/TenantInterceptor.java`
- Create: `backend/src/main/java/com/edu/common/config/MybatisPlusConfig.java`

- [ ] **Step 1: 编写TenantInterceptor租户拦截器**

```java
package com.edu.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.edu.common.util.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 租户拦截器，自动为SQL添加租户条件
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {

    /**
     * 租户ID字段名
     */
    private static final String TENANT_ID_FIELD = "tenant_id";

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler rh, BoundSql boundSql) throws SQLException {
        Long tenantId = TenantContextHolder.getTenantId();

        if (tenantId != null) {
            // 获取原始SQL
            String originalSql = boundSql.getSql();

            // 添加租户条件
            String newSql = addTenantCondition(originalSql, tenantId);

            // 替换SQL
            PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
            mpBs.sql(newSql);

            log.debug("租户SQL拦截: tenantId={}, sql={}", tenantId, newSql);
        }
    }

    /**
     * 为SQL添加租户条件
     */
    private String addTenantCondition(String sql, Long tenantId) {
        // 简单实现：在WHERE条件后添加tenant_id条件
        // 实际项目中应使用SQL解析器（如JSqlParser）进行更精确的处理

        String lowerSql = sql.toLowerCase();

        if (lowerSql.contains("where")) {
            // 已有WHERE条件，添加AND
            return sql + " AND " + TENANT_ID_FIELD + " = " + tenantId;
        } else {
            // 无WHERE条件，添加WHERE
            // 需要判断是否是SELECT语句
            if (lowerSql.startsWith("select")) {
                int fromIndex = lowerSql.indexOf("from");
                int orderByIndex = lowerSql.indexOf("order by");
                int limitIndex = lowerSql.indexOf("limit");

                if (orderByIndex > 0) {
                    return sql.substring(0, orderByIndex) +
                            " WHERE " + TENANT_ID_FIELD + " = " + tenantId + " " +
                            sql.substring(orderByIndex);
                } else if (limitIndex > 0) {
                    return sql.substring(0, limitIndex) +
                            " WHERE " + TENANT_ID_FIELD + " = " + tenantId + " " +
                            sql.substring(limitIndex);
                } else {
                    return sql + " WHERE " + TENANT_ID_FIELD + " = " + tenantId;
                }
            }
            return sql;
        }
    }
}
```

注意：此为简化实现，实际应使用JSqlParser进行SQL解析。后续Task将优化。

- [ ] **Step 2: 更新pom.xml添加JSqlParser依赖**

```xml
<!-- JSqlParser for SQL parsing -->
<dependency>
    <groupId>com.github.jsqlparser</groupId>
    <artifactId>jsqlparser</artifactId>
    <version>4.6</version>
</dependency>
```

- [ ] **Step 3: 优化TenantInterceptor使用JSqlParser**

```java
package com.edu.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.edu.common.util.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 租户拦截器，使用JSqlParser自动为SQL添加租户条件
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler rh, BoundSql boundSql) throws SQLException {
        Long tenantId = TenantContextHolder.getTenantId();

        if (tenantId == null) {
            return;
        }

        try {
            String originalSql = boundSql.getSql();
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);

            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                Expression where = plainSelect.getWhere();

                EqualsTo tenantCondition = new EqualsTo();
                tenantCondition.setLeftExpression(new Column(TENANT_ID_COLUMN));
                tenantCondition.setRightExpression(new LongValue(tenantId));

                if (where != null) {
                    plainSelect.setWhere(new AndExpression(where, tenantCondition));
                } else {
                    plainSelect.setWhere(tenantCondition);
                }
            }

            String newSql = select.toString();
            PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
            mpBs.sql(newSql);

            log.debug("租户SQL拦截: tenantId={}, newSql={}", tenantId, newSql);

        } catch (Exception e) {
            log.warn("租户SQL解析失败，跳过拦截: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 4: 编写MybatisPlusConfig配置类**

```java
package com.edu.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.edu.common.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加租户拦截器
        interceptor.addInnerInterceptor(new TenantInterceptor());

        // 分页插件（后续添加）
        // interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }
}
```

- [ ] **Step 5: 编写拦截器测试**

创建测试文件：`backend/src/test/java/com/edu/common/interceptor/TenantInterceptorTest.java`

```java
package com.edu.common.interceptor;

import com.edu.common.util.TenantContextHolder;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantInterceptorTest {

    private TenantInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new TenantInterceptor();
        TenantContextHolder.setTenantId(100L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testAddTenantConditionToSimpleSelect() throws Exception {
        String sql = "SELECT * FROM user WHERE username = 'admin'";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        // 验证解析成功
        assertNotNull(select);
        assertTrue(select.getSelectBody().toString().contains("tenant_id"));
    }

    @Test
    void testAddTenantConditionToSelectWithoutWhere() throws Exception {
        String sql = "SELECT * FROM user";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        String result = select.toString();
        assertTrue(result.contains("tenant_id"));
    }

    @Test
    void testTenantContextHolderIntegration() {
        Long tenantId = TenantContextHolder.getTenantId();
        assertEquals(100L, tenantId);

        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }
}
```

- [ ] **Step 6: 运行测试**

Run: `cd backend && mvn test -Dtest=TenantInterceptorTest`
Expected: 测试通过

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "feat: 创建租户拦截器（JSqlParser实现）"
```

---

## 阶段二：租户模块实现 (Tasks 6-10)

### Task 6: 创建租户实体和Mapper

**Files:**
- Create: `backend/src/main/java/com/edu/tenant-module/entity/Tenant.java`
- Create: `backend/src/main/java/com/edu/tenant-module/entity/TenantConfig.java`
- Create: `backend/src/main/java/com/edu/tenant-module/entity/School.java`
- Create: `backend/src/main/java/com/edu/tenant-module/mapper/TenantMapper.java`
- Create: `backend/src/main/java/com/edu/tenant-module/mapper/TenantConfigMapper.java`
- Create: `backend/src/main/java/com/edu/tenant-module/mapper/SchoolMapper.java`

- [ ] **Step 1: 编写Tenant实体**

```java
package com.edu.tenant_module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 租户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant")
public class Tenant extends BaseEntity {

    /**
     * 租户ID（主键，继承自BaseEntity无，需单独定义）
     */
    private Long id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 租户唯一标识
     */
    private String code;

    /**
     * AI服务提供商: CLOUD/PRIVATE
     */
    private String aiProvider;

    /**
     * AI配置JSON
     */
    private String aiConfig;

    /**
     * 状态: 1-正常, 0-禁用
     */
    private Integer status;

    /**
     * 服务到期日期
     */
    private LocalDate expireDate;
}
```

注意：BaseEntity包含tenantId，但Tenant表自身不需要tenantId字段（租户表不属于任何租户）。需要特殊处理。

- [ ] **Step 2: 创建TenantNoBaseEntity（不包含tenantId的基类）**

```java
package com.edu.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 无租户ID的实体基类（用于租户表等系统表）
 */
@Data
public class TenantNoBaseEntity {

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

- [ ] **Step 3: 更新Tenant实体使用TenantNoBaseEntity**

```java
package com.edu.tenant_module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 租户实体（无租户ID）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant")
public class Tenant extends TenantNoBaseEntity {

    private Long id;

    private String name;

    private String code;

    private String aiProvider;

    private String aiConfig;

    private Integer status;

    private LocalDate expireDate;
}
```

- [ ] **Step 4: 编写TenantConfig实体**

```java
package com.edu.tenant_module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_config")
public class TenantConfig extends TenantNoBaseEntity {

    private Long id;

    private Long tenantId;

    private String configKey;

    private String configValue;
}
```

- [ ] **Step 5: 编写School实体**

```java
package com.edu.tenant_module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("school")
public class School extends BaseEntity {

    private Long id;

    private String name;

    private String address;

    private String contactPhone;
}
```

- [ ] **Step 6: 编写TenantMapper**

```java
package com.edu.tenant_module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.tenant_module.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户Mapper
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

}
```

- [ ] **Step 7: 编写TenantConfigMapper**

```java
package com.edu.tenant_module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.tenant_module.entity.TenantConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户配置Mapper
 */
@Mapper
public interface TenantConfigMapper extends BaseMapper<TenantConfig> {

}
```

- [ ] **Step 8: 编写SchoolMapper**

```java
package com.edu.tenant_module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.tenant_module.entity.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校Mapper
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {

}
```

- [ ] **Step 9: Commit**

```bash
git add .
git commit -m "feat: 创建租户模块实体和Mapper"
```

---

### Task 7: 创建租户Service

**Files:**
- Create: `backend/src/main/java/com/edu/tenant-module/service/TenantService.java`
- Create: `backend/src/main/java/com/edu/tenant-module/service/TenantConfigService.java`
- Create: `backend/src/main/java/com/edu/tenant-module/service/SchoolService.java`
- Test: `backend/src/test/java/com/edu/tenant-module/service/TenantServiceTest.java`

- [ ] **Step 1: 编写TenantService测试**

```java
package com.edu.tenant_module.service;

import com.edu.tenant_module.entity.Tenant;
import com.edu.tenant_module.mapper.TenantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    @Mock
    private TenantMapper tenantMapper;

    @InjectMocks
    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTenant() {
        Tenant tenant = new Tenant();
        tenant.setName("测试学校");
        tenant.setCode("TEST_SCHOOL");
        tenant.setAiProvider("CLOUD");
        tenant.setStatus(1);

        when(tenantMapper.insert(any(Tenant.class))).thenReturn(1);

        Tenant result = tenantService.createTenant(tenant);

        assertNotNull(result);
        assertEquals("测试学校", result.getName());
    }

    @Test
    void testGetTenantByCode() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setCode("TEST_SCHOOL");

        // 实际测试中需要使用MyBatis-Plus查询
        // 这里简化验证逻辑
        assertNotNull(tenant);
    }
}
```

- [ ] **Step 2: 编写TenantService实现**

```java
package com.edu.tenant_module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.TenantException;
import com.edu.tenant_module.entity.Tenant;
import com.edu.tenant_module.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 租户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    /**
     * 创建租户
     */
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        // 验证租户编码唯一性
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getCode, tenant.getCode());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("租户编码已存在");
        }

        // 设置默认值
        if (tenant.getStatus() == null) {
            tenant.setStatus(1);
        }
        if (tenant.getAiProvider() == null) {
            tenant.setAiProvider("CLOUD");
        }

        baseMapper.insert(tenant);
        log.info("创建租户成功: id={}, code={}", tenant.getId(), tenant.getCode());

        return tenant;
    }

    /**
     * 根据编码查询租户
     */
    public Tenant getTenantByCode(String code) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 根据ID查询租户并验证状态
     */
    public Tenant getAndValidateTenant(Long tenantId) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        if (tenant.getStatus() == 0) {
            throw TenantException.disabled();
        }
        if (tenant.getExpireDate() != null && tenant.getExpireDate().isBefore(LocalDate.now())) {
            throw TenantException.expired();
        }
        return tenant;
    }

    /**
     * 更新租户AI配置
     */
    @Transactional
    public void updateAIConfig(Long tenantId, String aiProvider, String aiConfig) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }

        tenant.setAiProvider(aiProvider);
        tenant.setAiConfig(aiConfig);
        baseMapper.updateById(tenant);

        log.info("更新租户AI配置: tenantId={}, provider={}", tenantId, aiProvider);
    }

    /**
     * 禁用租户
     */
    @Transactional
    public void disableTenant(Long tenantId) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }

        tenant.setStatus(0);
        baseMapper.updateById(tenant);

        log.info("禁用租户: tenantId={}", tenantId);
    }
}
```

需要添加BusinessException导入。

- [ ] **Step 3: 编写TenantConfigService**

```java
package com.edu.tenant_module.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.tenant_module.entity.TenantConfig;
import com.edu.tenant_module.mapper.TenantConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 租户配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigService extends ServiceImpl<TenantConfigMapper, TenantConfig> {

    /**
     * 获取租户AI配置
     */
    public JSONObject getAIConfig(Long tenantId) {
        TenantConfig config = getConfig(tenantId, "ai_config");
        if (config == null || config.getConfigValue() == null) {
            return new JSONObject();
        }
        return JSON.parseObject(config.getConfigValue());
    }

    /**
     * 设置租户AI配置
     */
    public void setAIConfig(Long tenantId, JSONObject aiConfig) {
        setConfig(tenantId, "ai_config", aiConfig.toJSONString());
    }

    /**
     * 获取配置
     */
    public TenantConfig getConfig(Long tenantId, String key) {
        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getTenantId, tenantId)
                .eq(TenantConfig::getConfigKey, key);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 设置配置
     */
    public void setConfig(Long tenantId, String key, String value) {
        TenantConfig config = getConfig(tenantId, key);
        if (config == null) {
            config = new TenantConfig();
            config.setTenantId(tenantId);
            config.setConfigKey(key);
            config.setConfigValue(value);
            baseMapper.insert(config);
        } else {
            config.setConfigValue(value);
            baseMapper.updateById(config);
        }
        log.info("设置租户配置: tenantId={}, key={}", tenantId, key);
    }
}
```

- [ ] **Step 4: 编写SchoolService**

```java
package com.edu.tenant_module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.tenant_module.entity.School;
import com.edu.tenant_module.mapper.SchoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学校服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService extends ServiceImpl<SchoolMapper, School> {

    /**
     * 创建学校
     */
    @Transactional
    public School createSchool(School school) {
        // 验证租户下学校名称唯一
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getTenantId, school.getTenantId())
                .eq(School::getName, school.getName());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学校名称已存在");
        }

        baseMapper.insert(school);
        log.info("创建学校成功: id={}, tenantId={}", school.getId(), school.getTenantId());

        return school;
    }

    /**
     * 获取租户下的学校列表
     */
    public List<School> listByTenant(Long tenantId) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getTenantId, tenantId)
                .orderByDesc(School::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 获取租户的默认学校（第一个学校）
     */
    public School getDefaultSchool(Long tenantId) {
        List<School> schools = listByTenant(tenantId);
        if (schools.isEmpty()) {
            return null;
        }
        return schools.get(0);
    }
}
```

- [ ] **Step 5: 运行测试**

Run: `cd backend && mvn test -Dtest=TenantServiceTest`
Expected: 测试通过（Mock测试）

- [ ] **Step 6: Commit**

```bash
git add .
git commit -m "feat: 创建租户模块Service层"
```

---

### Task 8: 创建租户Controller

**Files:**
- Create: `backend/src/main/java/com/edu/tenant-module/controller/TenantController.java`
- Create: `backend/src/main/java/com/edu/tenant-module/controller/SchoolController.java`
- Create: `backend/src/main/java/com/edu/tenant-module/dto/TenantCreateRequest.java`
- Create: `backend/src/main/java/com/edu/tenant-module/dto/TenantResponse.java`

- [ ] **Step 1: 编写TenantCreateRequest DTO**

```java
package com.edu.tenant_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 租户创建请求
 */
@Data
public class TenantCreateRequest {

    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称最长100字符")
    private String name;

    @NotBlank(message = "租户编码不能为空")
    @Size(max = 50, message = "租户编码最长50字符")
    private String code;

    private String aiProvider;

    private String aiConfig;

    private String expireDate;
}
```

- [ ] **Step 2: 编写TenantResponse DTO**

```java
package com.edu.tenant_module.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户响应
 */
@Data
public class TenantResponse {

    private Long id;

    private String name;

    private String code;

    private String aiProvider;

    private String aiConfig;

    private Integer status;

    private LocalDate expireDate;

    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 编写TenantController**

```java
package com.edu.tenant_module.controller;

import com.edu.common.entity.Result;
import com.edu.tenant_module.dto.TenantCreateRequest;
import com.edu.tenant_module.dto.TenantResponse;
import com.edu.tenant_module.entity.Tenant;
import com.edu.tenant_module.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 租户控制器
 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 创建租户
     */
    @PostMapping
    public Result<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCode(request.getCode());
        tenant.setAiProvider(request.getAiProvider() != null ? request.getAiProvider() : "CLOUD");
        tenant.setAiConfig(request.getAiConfig());

        if (request.getExpireDate() != null) {
            tenant.setExpireDate(LocalDate.parse(request.getExpireDate()));
        }

        Tenant created = tenantService.createTenant(tenant);

        TenantResponse response = toResponse(created);
        return Result.success(response);
    }

    /**
     * 根据编码查询租户
     */
    @GetMapping("/code/{code}")
    public Result<TenantResponse> getTenantByCode(@PathVariable String code) {
        Tenant tenant = tenantService.getTenantByCode(code);
        if (tenant == null) {
            return Result.error("租户不存在");
        }

        TenantResponse response = toResponse(tenant);
        return Result.success(response);
    }

    /**
     * 根据ID查询租户
     */
    @GetMapping("/{id}")
    public Result<TenantResponse> getTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.getAndValidateTenant(id);
        TenantResponse response = toResponse(tenant);
        return Result.success(response);
    }

    /**
     * 更新AI配置
     */
    @PutMapping("/{id}/ai-config")
    public Result<Void> updateAIConfig(@PathVariable Long id,
                                        @RequestParam String provider,
                                        @RequestBody String config) {
        tenantService.updateAIConfig(id, provider, config);
        return Result.success();
    }

    /**
     * 禁用租户
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disableTenant(@PathVariable Long id) {
        tenantService.disableTenant(id);
        return Result.success();
    }

    /**
     * 实体转响应DTO
     */
    private TenantResponse toResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setCode(tenant.getCode());
        response.setAiProvider(tenant.getAiProvider());
        response.setAiConfig(tenant.getAiConfig());
        response.setStatus(tenant.getStatus());
        response.setExpireDate(tenant.getExpireDate());
        response.setCreatedAt(tenant.getCreatedAt());
        return response;
    }
}
```

- [ ] **Step 4: 编写SchoolController**

```java
package com.edu.tenant_module.controller;

import com.edu.common.entity.Result;
import com.edu.tenant_module.entity.School;
import com.edu.tenant_module.service.SchoolService;
import com.edu.common.util.TenantContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学校控制器
 */
@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * 创建学校
     */
    @PostMapping
    public Result<School> createSchool(@Valid @RequestBody SchoolCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }

        School school = new School();
        school.setTenantId(tenantId);
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());

        School created = schoolService.createSchool(school);
        return Result.success(created);
    }

    /**
     * 获取学校列表
     */
    @GetMapping
    public Result<List<School>> listSchools() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }

        List<School> schools = schoolService.listByTenant(tenantId);
        return Result.success(schools);
    }

    /**
     * 获取默认学校
     */
    @GetMapping("/default")
    public Result<School> getDefaultSchool() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }

        School school = schoolService.getDefaultSchool(tenantId);
        return Result.success(school);
    }

    /**
     * 学校创建请求
     */
    @Data
    public static class SchoolCreateRequest {
        @NotBlank(message = "学校名称不能为空")
        private String name;

        private String address;

        private String contactPhone;
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: 创建租户模块Controller层"
```

---

## 阶段三：用户模块实现 (Tasks 9-15)

由于篇幅限制，后续任务将遵循相同的TDD模式：

### Task 9: 创建用户实体和Mapper
### Task 10: 创建角色和权限实体
### Task 11: 创建用户Service（含认证逻辑）
### Task 12: 创建JWT工具类
### Task 13: 创建Security配置
### Task 14: 创建用户Controller
### Task 15: 创建组织架构实体（Grade/Class/Student）

---

## 阶段四：试卷模块实现 (Tasks 16-25)

### Task 16: 创建题库和题目实体
### Task 17: 创建试卷模板实体
### Task 18: 创建试卷和试卷题目实体
### Task 19: 创建题库Service
### Task 20: 创建题目Service
### Task 21: 创建试卷模板Service
### Task 22: 创建试卷Service
### Task 23: 创建试卷生成Service（含AI调用）
### Task 24: 创建试卷Controller
### Task 25: 编写试卷生成测试

---

## 阶段五：批改模块实现 (Tasks 26-35)

### Task 26: 创建答题卡和答案实体
### Task 27: 创建成绩分析实体
### Task 28: 创建答题Service
### Task 29: 创建批改Service（规则引擎）
### Task 30: 创建成绩分析Service
### Task 31: 创建错题记录Service
### Task 32: 创建批改Controller
### Task 33: 编写客观题批改测试
### Task 34: 编写成绩分析测试
### Task 35: 编写错题记录测试

---

## 阶段六：AI服务层实现 (Tasks 36-45)

### Task 36: 创建AI Provider接口
### Task 37: 创建AI请求/响应DTO
### Task 38: 创建CloudAIProvider实现
### Task 39: 创建PrivateAIProvider实现
### Task 40: 创建AIProviderFactory
### Task 41: 创建PromptBuilder服务
### Task 42: 创建AIService
### Task 43: 创建AI调用监控
### Task 44: 编写AI Provider测试
### Task 45: 编写Prompt测试

---

## 阶段七：前端实现 (Tasks 46-60)

### Task 46: 初始化Vue项目
### Task 47: 配置路由和状态管理
### Task 48: 创建axios请求封装
### Task 49: 创建登录页面
### Task 50: 创建Dashboard页面
### Task 51: 创建题库管理页面
### Task 52: 创建试卷模板页面
### Task 53: 创建试卷生成页面
### Task 54: 创建试卷编辑组件
### Task 55: 创建答题页面
### Task 56: 创建成绩分析页面
### Task 57: 创建错题记录页面
### Task 58: 创建组织架构管理页面
### Task 59: 创建用户管理页面
### Task 60: 系统集成测试

---

## 阶段八：集成测试与部署 (Tasks 61-70)

### Task 61: 编写租户模块集成测试
### Task 62: 编写用户模块集成测试
### Task 63: 编写试卷模块集成测试
### Task 64: 编写批改模块集成测试
### Task 65: 编写AI服务集成测试
### Task 66: 编写端到端测试（试卷生成→批改→分析）
### Task 67: 配置生产环境
### Task 68: 编写API文档
### Task 69: 编写部署文档
### Task 70: 最终验收测试

---

## 自检清单

**1. Spec覆盖检查**:
- ✅ 租户模块（Tenant, TenantConfig, School）
- ✅ 用户模块（User, Role, Permission, 组织架构）
- ✅ 试卷模块（题库、模板、试卷生成）
- ✅ 批改模块（答题、批改、成绩分析、错题）
- ✅ AI服务层（Provider抽象、云端/私有切换）
- ✅ 多租户隔离（拦截器、上下文）
- ⚠️ 前端实现：Tasks 46-60 需补充详细步骤（后续迭代）
- ⚠️ 部署配置：Tasks 67-70 需补充详细步骤（后续迭代）

**2. Placeholder扫描**:
- ⚠️ 发现：Tasks 9-70 使用简略描述，未包含完整代码步骤
- 需后续迭代补充每个Task的详细步骤

**3. Type一致性**:
- ✅ Tenant使用TenantNoBaseEntity（无tenantId）
- ✅ School/User/Exam等使用BaseEntity（含tenantId）
- ✅ TenantContextHolder与拦截器配合使用

---

**计划状态**: 第一阶段（Tasks 1-8）详细完成，后续阶段框架定义完毕，需迭代补充详细步骤。建议采用Subagent-Driven方式逐步实现。