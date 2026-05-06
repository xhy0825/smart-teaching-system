---
name: 教师智能教学系统设计
description: 教师专用的备课、试卷生成、试卷批改、PPT制作、学生画像、个性化出题等功能的SaaS平台设计
type: project
---

# 教师智能教学系统设计文档

**版本**: v1.0
**日期**: 2026-05-06
**状态**: 设计评审完成

---

## 一、项目概述

### 1.1 项目定位

教师智能教学系统是一个面向K12教育场景的SaaS平台，为教师提供智能化的教学辅助工具，包括试卷生成、试卷批改等核心功能，通过AI技术提升教师工作效率。

### 1.2 目标用户

- **主要用户**: K12学校教师（数理化英四科）
- **客户群体**: 公立学校、私立学校、培训机构
- **终端用户**: 学校管理员、教师、学生、家长

### 1.3 MVP核心功能

**第一版（MVP）聚焦**:
- 试卷生成模块
- 试卷批改模块

**后续扩展模块**:
- 备课模块
- PPT制作模块
- 学生画像模块
- 个性化出题模块

---

## 二、技术架构

### 2.1 架构选型

| 层级 | 技术选型 | 决策依据 |
|------|---------|---------|
| **前端** | Vue 3 + Element Plus | 国内教育产品主流，组件库成熟，开发效率高 |
| **后端** | Spring Boot 3.x + Java 17 | 企业级成熟方案，复杂业务逻辑支持强 |
| **数据库** | MySQL 8.x + Redis 7.x | 关系数据存储 + 缓存热点，SaaS标配 |
| **AI服务** | 混合方案（云端API + 私有部署） | 满足差异化客户需求，架构可扩展 |
| **文件存储** | MinIO / OSS | 试卷附件、答题图片存储 |
| **架构模式** | 模块化单体架构 | MVP阶段开发效率高，后期可拆分演进 |

### 2.2 分层结构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3 SPA)                    │
│  Element Plus + 富文本编辑器 + 试卷编辑组件                   │
└─────────────────────────────────────────────────────────────┘
                              ↓ REST API (JWT)
┌─────────────────────────────────────────────────────────────┐
│                       后端层 (Spring Boot)                   │
├─────────────────────────────────────────────────────────────┤
│  tenant-module      多租户管理、租户隔离                      │
│  user-module        用户管理、组织架构、权限                   │
│  exam-module        试卷生成、题库管理、模板系统               │
│  grading-module     答题管理、批改处理、成绩分析               │
│  ai-module          AI服务抽象层                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  MySQL (核心业务) + Redis (缓存/会话/计数)                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  云端AI API (Claude/GPT/文心) 或 私有部署AI服务                │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 模块职责划分

| 模块 | 核心职责 | 主要实体 |
|------|---------|---------|
| tenant-module | 租户注册、租户隔离、租户配置（AI方案选择） | Tenant, TenantConfig |
| user-module | 用户注册、角色管理、组织架构、权限控制 | User, Role, School, Grade, Class, Permission |
| exam-module | 题库管理、试卷模板、AI试卷生成、试卷发布 | QuestionBank, Question, ExamPaper, ExamTemplate |
| grading-module | 答题记录、批改处理、成绩统计、错题分析 | AnswerSheet, Answer, GradingResult, ScoreAnalysis |
| ai-module | AI服务接口抽象、云端API调用、私有服务调用 | 无实体，纯服务层 |

---

## 三、数据模型设计

### 3.1 核心实体关系

```
Tenant (租户)
    │
    ├─── School (学校) ─── Grade (年级) ─── Class (班级) ─── Student (学生)
    │
    ├─── User (用户) ──── Role (角色) ──── Permission (权限)
    │
    ├─── QuestionBank (题库) ──── Question (题目)
    │
    ├─── ExamTemplate (试卷模板)
    │
    ├─── ExamPaper (试卷) ──── ExamQuestion (试卷题目)
    │                    │
    │                    └─── AnswerSheet (答题卡) ─── Answer (答案)
    │                                              │
    │                                              └─── GradingResult (批改结果)
    │
    └─── StudentProfile (学生画像，第二版)
```

### 3.2 多租户隔离策略

采用 **共享数据库 + 租户ID字段隔离** 方案:
- 所有业务表包含 `tenant_id` 字段
- MyBatis-Plus拦截器自动添加租户条件
- Redis缓存按租户ID隔离命名空间

### 3.3 核心表结构

#### 租户模块

```sql
CREATE TABLE tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '租户唯一标识',
    ai_provider VARCHAR(20) COMMENT 'AI服务提供商: CLOUD/PRIVATE',
    ai_config JSON COMMENT 'AI配置',
    status TINYINT DEFAULT 1,
    expire_date DATE COMMENT '服务到期日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tenant_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(50) NOT NULL,
    config_value JSON,
    UNIQUE KEY uk_tenant_key (tenant_id, config_key)
);
```

#### 用户模块

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(200),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_username (tenant_id, username)
);

CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT 'ADMIN/TEACHER/STUDENT/PARENT',
    code VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    UNIQUE KEY uk_tenant_code (tenant_id, code)
);

CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL COMMENT 'exam:create/grading:view等',
    name VARCHAR(50) NOT NULL,
    resource VARCHAR(50),
    action VARCHAR(20) COMMENT 'create/read/update/delete'
);
```

#### 组织架构模块

```sql
CREATE TABLE school (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact_phone VARCHAR(20)
);

CREATE TABLE grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '七年级/高一',
    level TINYINT COMMENT '1-小学, 2-初中, 3-高中',
    sequence INT
);

CREATE TABLE class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grade_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    teacher_id BIGINT COMMENT '班主任ID',
    student_count INT DEFAULT 0
);

CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '关联用户账号',
    name VARCHAR(50) NOT NULL,
    student_no VARCHAR(20),
    gender TINYINT,
    birth_date DATE,
    status TINYINT DEFAULT 1,
    UNIQUE KEY uk_tenant_student_no (tenant_id, student_no)
);
```

#### 试卷模块

```sql
CREATE TABLE question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(20) NOT NULL COMMENT 'MATH/PHYSICS/CHEMISTRY/ENGLISH',
    grade_level TINYINT,
    description VARCHAR(200),
    is_public TINYINT DEFAULT 0,
    created_by BIGINT
);

CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_id BIGINT NOT NULL,
    subject VARCHAR(20) NOT NULL,
    question_type VARCHAR(20) NOT NULL COMMENT 'CHOICE/FILL/JUDGE/CALCULATION',
    difficulty TINYINT COMMENT '1-简单, 2-中等, 3-困难',
    content TEXT NOT NULL,
    options JSON COMMENT '选择题选项',
    answer TEXT NOT NULL,
    answer_analysis TEXT,
    knowledge_points JSON,
    source VARCHAR(50) COMMENT 'MANUAL/AI_GENERATED',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(20) NOT NULL,
    total_score DECIMAL(5,2),
    time_limit INT,
    structure JSON COMMENT '试卷结构配置',
    created_by BIGINT
);

CREATE TABLE exam_paper (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    template_id BIGINT,
    title VARCHAR(100) NOT NULL,
    subject VARCHAR(20) NOT NULL,
    grade_id BIGINT,
    class_id BIGINT,
    total_score DECIMAL(5,2) NOT NULL,
    time_limit INT,
    status TINYINT DEFAULT 0 COMMENT '0-草稿, 1-已发布, 2-考试中, 3-已结束, 4-已批改',
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME
);

CREATE TABLE exam_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    sequence INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    UNIQUE KEY uk_exam_question (exam_paper_id, sequence)
);
```

#### 批改模块

```sql
CREATE TABLE answer_sheet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    exam_paper_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-未提交, 1-已提交, 2-批改中, 3-已批改',
    total_score DECIMAL(5,2),
    submit_time DATETIME,
    grading_time DATETIME,
    graded_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_student (exam_paper_id, student_id)
);

CREATE TABLE answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    answer_sheet_id BIGINT NOT NULL,
    exam_question_id BIGINT NOT NULL,
    student_answer TEXT,
    is_correct TINYINT COMMENT '0-错误, 1-正确, 2-部分正确',
    score DECIMAL(5,2),
    ai_score DECIMAL(5,2),
    manual_score DECIMAL(5,2),
    ai_analysis TEXT,
    graded_at DATETIME,
    UNIQUE KEY uk_sheet_question (answer_sheet_id, exam_question_id)
);

CREATE TABLE score_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_paper_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    avg_score DECIMAL(5,2),
    max_score DECIMAL(5,2),
    min_score DECIMAL(5,2),
    pass_rate DECIMAL(5,4),
    excellent_rate DECIMAL(5,4),
    question_analysis JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_class (exam_paper_id, class_id)
);

CREATE TABLE student_wrong_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    exam_paper_id BIGINT,
    wrong_count INT DEFAULT 1,
    last_wrong_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    corrected_at DATETIME,
    UNIQUE KEY uk_student_question (student_id, question_id)
);
```

---

## 四、试卷生成模块设计

### 4.1 三种生成方式

| 方式 | 适用场景 | AI参与度 |
|------|---------|---------|
| **模板生成** | 标准化考试（期中期末） | 低，仅补充不足题目 |
| **智能生成** | 日常测验/作业 | 高，AI主导选题和生成 |
| **手动组卷** | 精细化备课 | 无，仅题库查询 |

### 4.2 AI试卷生成流程

1. 教师配置参数（学科、题型、难度、知识点）
2. 系统从题库筛选匹配题目
3. 题库不足时调用AI生成补充
4. 按结构组装试卷返回预览
5. 教师审核/调整后保存发布

### 4.3 题库管理

- 题目录入：手动添加 + 批量导入（Excel）
- 题目分类：学科、题型、难度、知识点标签
- 题目共享：租户内公开题库支持跨班级共享

### 4.4 试卷模板

模板结构定义示例:
```json
{
  "name": "初中数学单元测试模板",
  "subject": "MATH",
  "totalScore": 100,
  "timeLimit": 45,
  "structure": [
    {"section": "选择题", "type": "CHOICE", "count": 10, "scoreEach": 4},
    {"section": "填空题", "type": "FILL", "count": 5, "scoreEach": 4},
    {"section": "计算题", "type": "CALCULATION", "count": 3, "scoreEach": 12}
  ]
}
```

### 4.5 AI生成质量保障

| 环节 | 机制 |
|------|------|
| 生成前 | 知识点校验，确保匹配 |
| 生成后 | 格式校验（答案格式、选项完整性） |
| 教师审核 | 人工复核，可替换/删除不合格题目 |
| 题库沉淀 | 教师认可的AI题目自动入库 |

---

## 五、试卷批改模块设计

### 5.1 批改方式分层

| 题型 | 批改方式 | 准确率 | 人工复核 |
|------|---------|--------|---------|
| 选择题 | 规则引擎（字符串匹配） | 100% | 无需 |
| 填空题 | 规则引擎（多答案/模糊匹配） | 95%+ | 可选 |
| 判断题 | 规则引擎（布尔值匹配） | 100% | 无需 |
| 计算题 | 规则引擎（数值答案匹配） | 90%+ | 推荐 |

### 5.2 第一版支持范围

- ✅ 在线答题 → 系统直接批改
- ✅ 批量导入成绩 → Excel导入后统计分析
- ❌ 拍照上传 → OCR识别（第二版）

### 5.3 成绩分析

**班级级统计**:
- 平均分、最高分、最低分
- 及格率、优秀率
- 分数段分布

**题目级分析**:
- 各题得分率
- 高频错误答案统计

**学生级分析**:
- 个人排名
- 错题清单
- 知识点薄弱项识别

### 5.4 错题数据沉淀

- 每道错题自动记录到 `student_wrong_question` 表
- 统计错题次数，识别高频错题
- 关联知识点，为第二版学生画像提供数据基础

### 5.5 教师复核机制

- 填空题/计算题批改后，教师可选择复核
- 学生申诉成绩有误时触发
- 复核后系统重新统计班级分析

---

## 六、多租户和权限设计

### 6.1 组织架构

```
Tenant (租户/SaaS客户)
    └─── School (学校)
         └─── Grade (年级) ─── Class (班级)
              ├─── Teacher (教师)
              └─── Student (学生)
```

### 6.2 角色定义

| 角色 | 编码 | 说明 |
|------|------|------|
| 租户管理员 | ADMIN | 学校教务主任、校长 |
| 教师 | TEACHER | 任课教师 |
| 学生 | STUDENT | 学生账号 |
| 家长 | PARENT | 家长账号 |

### 6.3 核心权限点

| 权限编码 | 适用角色 |
|---------|---------|
| tenant:manage | ADMIN |
| user:create | ADMIN |
| class:manage | ADMIN |
| question:create | ADMIN, TEACHER |
| exam:create | ADMIN, TEACHER |
| exam:publish | ADMIN, TEACHER |
| grading:view | ALL |
| grading:review | ADMIN, TEACHER |
| score:view | ALL |
| score:export | ADMIN, TEACHER |

### 6.4 数据权限边界

**教师**: 仅访问所任教班级数据
**学生**: 仅访问个人成绩和错题
**家长**: 仅访问关联孩子数据

---

## 七、AI服务层设计

### 7.1 AI服务抽象层

```java
public interface AIProvider {
    QuestionGenerateResult generateQuestion(QuestionGenerateRequest request);
    GradingResult gradeSubjectiveQuestion(GradingRequest request);
    ServiceStatus checkStatus();
}
```

### 7.2 多Provider支持

| Provider | 实现方式 | 适用场景 |
|---------|---------|---------|
| CloudAIProvider | Claude/GPT/文心API | 云端方案 |
| PrivateAIProvider | 私有部署HTTP接口 | 私有方案 |

### 7.3 动态Provider选择

```java
public AIProvider getProvider(Long tenantId) {
    TenantConfig config = tenantConfigService.getAIConfig(tenantId);
    switch (config.getAiProvider()) {
        case "CLOUD": return cloudAIProvider;
        case "PRIVATE": return privateAIProvider;
    }
}
```

### 7.4 租户AI配置

```json
{
  "ai_provider": "CLOUD",
  "ai_config": {
    "provider_name": "Claude",
    "api_key": "sk-xxx",
    "model": "claude-sonnet-4-6"
  }
}
```

### 7.5 AI调用监控

Redis计数器记录API调用次数和Token消耗，用于:
- 租户使用量监控
- 计费依据
- 异常检测

---

## 八、MVP范围确认

### 8.1 第一版功能清单

**试卷生成模块**:
- 题库管理（录入、导入、分类）
- 试卷模板管理
- AI试卷生成（数理化英四科）
- 试卷编辑器（富文本、拖拽排序）
- 试卷发布与分发

**试卷批改模块**:
- 在线答题收集
- 客观题自动批改（规则引擎）
- 成绩统计分析（班级/学生）
- 错题记录沉淀
- 教师复核功能

**基础架构**:
- 多租户隔离
- 组织架构管理
- 用户权限体系
- AI服务抽象层

### 8.2 第一版暂不实现

- 拍照上传 + OCR识别
- 主观题AI批改（证明题、论述题）
- 备课模块
- PPT制作模块
- 学生画像模块
- 个性化出题模块

### 8.3 后续版本规划

**第二版**:
- OCR识别 → 线下纸质试卷拍照批改
- 主观题AI批改 → 计算题步骤评分
- 学生画像 → 知识结构、薄弱项分析

**第三版**:
- 备课模块
- PPT制作模块
- 个性化出题 → 根据画像智能推荐

---

## 九、技术风险与应对

### 9.1 AI生成质量风险

**风险**: AI生成题目质量不稳定，可能存在错误或不合理内容
**应对**:
- 教师审核机制强制复核
- 题库沉淀逐步减少AI依赖
- Prompt优化提升生成质量

### 9.2 多租户数据安全风险

**风险**: 跨租户数据泄露
**应对**:
- MyBatis-Plus拦截器自动注入租户条件
- API层权限校验双重保障
- 定期安全审计

### 9.3 AI服务可用性风险

**风险**: 云端API调用失败或超时
**应对**:
- 多Provider备选（Claude/GPT/文心）
- 重试机制（最多3次）
- 私有部署方案作为高可用备选

---

## 十、部署架构

### 10.1 云端部署（标准SaaS）

```
┌─────────────────────────────────────────────┐
│  用户浏览器                                  │
└─────────────────────────────────────────────┘
                    ↓ HTTPS
┌─────────────────────────────────────────────┐
│  Nginx (负载均衡 + SSL)                      │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Spring Boot 应用服务器集群                  │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  MySQL (主从复制) + Redis (集群)             │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  云端AI API / OSS文件存储                    │
└─────────────────────────────────────────────┘
```

### 10.2 私有部署方案

- 应用服务器 + 数据库 + Redis 私有部署
- AI服务私有部署（Qwen/Llama开源模型）
- MinIO本地文件存储
- 数据完全不出域

---

## 附录：第二版扩展预留

| 功能 | 第一版状态 | 第二版扩展方案 |
|------|-----------|---------------|
| OCR识别 | ❌ | 百度/腾讯OCR API + 手写识别 |
| 主观题批改 | ❌ | AI步骤评分 + 人工复核 |
| 学生画像 | ❌ | AI分析错题数据生成画像 |
| 个性化出题 | ❌ | 根据画像推荐薄弱知识点题目 |
| 备课模块 | ❌ | 富文本备课资料编辑 + AI辅助 |
| PPT制作 | ❌ | PPTX导出 + AI内容生成 |