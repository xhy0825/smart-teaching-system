---
name: 模拟数据SQL脚本设计
description: 为教师智能教学系统生成1000条接近真实的模拟数据，用于测试和预览系统
type: project
---

# 模拟数据 SQL 脚本设计文档

**版本**: v1.0
**日期**: 2026-05-08
**状态**: 设计完成

---

## 一、目标

为教师智能教学系统生成接近真实的模拟数据，便于测试和预览系统功能。

---

## 二、数据分配方案

按业务关系合理分配，核心表达到1000条：

| 表名 | 数量 | 说明 |
|------|------|------|
| Tenant | 3 | 3个租户/学校，可切换租户测试多租户功能 |
| School | 3 | 每个租户1所学校 |
| Grade | 9 | 每校3个年级（七/八/九年级） |
| Class | 30 | 每年级约3个班级，每班40人 |
| **Student** | **1000** | 核心数据，每班约33-40名学生 |
| User | 50 | 30个教师 + 3个管理员 + 17个学生账号 |
| Role | 12 | 每租户4个角色（ADMIN/TEACHER/STUDENT/PARENT） |
| Permission | 10 | 保持现有权限不变 |
| UserRole | 50 | 用户角色关联 |
| RolePermission | ~30 | 角色权限关联 |
| QuestionBank | 12 | 每租户4个题库（数理化英） |
| **Question** | **1000** | 核心数据，每题库约83题 |
| ExamTemplate | 12 | 每租户4个模板 |
| ExamPaper | 30 | 已发布试卷，每班约1张 |
| ExamQuestion | ~900 | 每张试卷约30题 |
| **AnswerSheet** | **1000** | 核心数据，每学生约1份答题卡 |
| Answer | ~30000 | 每答题卡约30个答案 |
| ScoreAnalysis | 30 | 每班级每试卷1条分析记录 |
| StudentWrongQuestion | ~500 | 学生错题记录 |

---

## 三、文件结构

生成文件：`backend/src/main/resources/db/mock-data.sql`

脚本内容按模块分组：
```sql
-- ============================================================
-- 模拟数据 - 用于测试和预览系统
-- ============================================================

-- 租户模块 (Tenant)
-- 学校模块 (School)
-- 用户模块 (User, Role, UserRole, RolePermission)
-- 组织架构 (Grade, Class, Student)
-- 试卷模块 (QuestionBank, Question, ExamTemplate, ExamPaper, ExamQuestion)
-- 批改模块 (AnswerSheet, Answer, ScoreAnalysis, StudentWrongQuestion)
```

---

## 四、数据生成规则

### 4.1 Tenant（3条）

| ID | 名称 | 编码 | AI提供商 | 状态 |
|----|------|------|---------|------|
| 1 | 测试学校 | TEST_SCHOOL | CLOUD | 1 |
| 2 | 示范中学 | DEMO_SCHOOL | CLOUD | 1 |
| 3 | 实验学校 | EXP_SCHOOL | PRIVATE | 1 |

### 4.2 School（3条）

| ID | 租户ID | 名称 | 地址 |
|----|--------|------|------|
| 1 | 1 | 测试学校 | 北京市海淀区测试路1号 |
| 2 | 2 | 示范中学 | 上海市浦东新区示范街2号 |
| 3 | 3 | 实验学校 | 广州市天河区实验大道3号 |

### 4.3 Grade（9条）

每个学校3个年级：
- 学校1: 七年级(ID:1)、八年级(ID:2)、九年级(ID:3)
- 学校2: 七年级(ID:4)、八年级(ID:5)、九年级(ID:6)
- 学校3: 七年级(ID:7)、八年级(ID:8)、九年级(ID:9)

字段：school_id, name, level(2-初中), sequence

### 4.4 Class（30条）

每个年级3个班级：
- 每年级3个班级：一班、二班、三班
- ID: 1-30，按年级顺序分配
- student_count: 初始为0，生成学生后统计

### 4.5 Student（1000条）

- ID: 1-1000
- tenant_id: 按班级所属学校分配
- class_id: 按班级均匀分配，每班约33-40人
- name: 随机中文名（从常见姓氏+名字池生成）
- student_no: 格式 `2024001` ~ `20241000`
- gender: 随机1（男）/2（女），比例约1:1
- status: 1（在读）
- deleted: 0

### 4.6 User（50条）

| 类型 | 数量 | 用户名格式 | 说明 |
|------|------|-----------|------|
| 管理员 | 3 | admin1/admin2/admin3 | 每租户1个 |
| 教师 | 30 | teacher01~teacher30 | 每租户10个，tenant_id按租户分配 |
| 学生账号 | 17 | student001~student017 | 关联前17个学生用于登录测试 |

密码统一：BCrypt加密后的 `mock123`

字段：tenant_id, username, password, real_name, status(1), deleted(0)

### 4.7 Role（12条）

每租户4个角色：
- ID: 1-4(租户1), 5-8(租户2), 9-12(租户3)
- code: ADMIN/TEACHER/STUDENT/PARENT
- name: 管理员/教师/学生/家长

### 4.8 UserRole（50条）

- 3个管理员 → ADMIN角色
- 30个教师 → TEACHER角色
- 17个学生账号 → STUDENT角色

### 4.9 RolePermission（保持现有）

复用 init-data.sql 中的权限和角色权限关联数据。

### 4.10 QuestionBank（12条）

每个租户4个题库：
- ID: 1-4(租户1), 5-8(租户2), 9-12(租户3)
- subject: MATH/PHYSICS/CHEMISTRY/ENGLISH
- name: 初中数学题库/初中物理题库/初中化学题库/初中英语题库
- grade_level: 2（初中）
- is_public: 1（租户内公开）
- created_by: 对应租户的admin用户

### 4.11 Question（1000条）

学科分布：
- MATH: 300题（题库1,5,9各100题）
- PHYSICS: 200题（题库2,6,10各67题）
- CHEMISTRY: 200题（题库3,7,11各67题）
- ENGLISH: 200题（题库4,8,12各67题）

难度分布：
- 简单(difficulty=1): 40%
- 中等(difficulty=2): 40%
- 困难(difficulty=3): 20%

题型分布：
- 选择题(CHOICE): 50%
- 填空题(FILL): 30%
- 判断题(JUDGE): 10%
- 计算题(CALCULATION): 10%

内容生成规则：
- 按学科生成真实题目内容示例
- 选择题：4个选项(A/B/C/D)
- 知识点：JSON数组格式

### 4.12 ExamTemplate（12条）

每个租户4个模板：
- subject: MATH/PHYSICS/CHEMISTRY/ENGLISH
- total_score: 100
- time_limit: 45分钟
- structure: JSON格式的试卷结构配置

### 4.13 ExamPaper（30条）

- 每个班级1张已发布试卷（共30张）
- ID: 1-30
- tenant_id: 按班级所属租户分配
- class_id: 1-30 对应班级
- title: 格式如"七年级一班数学测试"
- subject: 按班级所属年级决定（七年级用MATH）
- total_score: 100
- time_limit: 45
- status: 4（已批改）
- created_by: 对应租户的admin用户

### 4.14 ExamQuestion（约900条）

- 每张试卷30题
- 从对应题库随机选取题目
- sequence: 1-30
- score: 按题型分配（选择题4分，填空题4分，计算题12分）

### 4.15 AnswerSheet（1000条）

- 每个学生对应一张答题卡
- ID: 1-1000
- exam_paper_id: 按学生班级分配对应试卷
- student_id: 1-1000
- status: 3（已批改）
- total_score: 随机生成60-100分（正态分布偏向80分）
- submit_time: 随机日期时间
- grading_time: submit_time + 1小时
- graded_by: 对应租户的teacher用户

### 4.16 Answer（约30000条）

- 每答题卡30个答案
- student_answer: 随机生成答案
- is_correct: 随机分布（正确率约70%）
- score: 根据is_correct和题目分值计算
- ai_score: 与score相同（模拟AI批改）
- graded_at: 与answer_sheet.grading_time相同

### 4.17 ScoreAnalysis（30条）

- 每班级每试卷1条
- 计算班级统计数据：
  - avg_score: 从该班级学生答题卡计算
  - max_score/min_score: 最高分/最低分
  - pass_rate: 及格率（>=60分）
  - excellent_rate: 优秀率（>=85分）

### 4.18 StudentWrongQuestion（约500条）

- 从Answer中筛选is_correct=0的记录
- wrong_count: 1-3随机
- 关联student_id和question_id

---

## 五、ID生成策略

使用固定ID序列，确保外键引用正确：

| 表 | ID范围 |
|----|--------|
| Tenant | 1-3 |
| School | 1-3 |
| Grade | 1-9 |
| Class | 1-30 |
| Student | 1-1000 |
| User | 1-50 |
| Role | 1-12 |
| QuestionBank | 1-12 |
| Question | 1-1000 |
| ExamTemplate | 1-12 |
| ExamPaper | 1-30 |
| ExamQuestion | 1-900 |
| AnswerSheet | 1-1000 |
| Answer | 1-30000 |
| ScoreAnalysis | 1-30 |

---

## 六、多租户数据分配

| 租户ID | 包含数据 |
|--------|---------|
| 1 | School1, Grade1-3, Class1-9, Student1-333, User(admin1,teacher01-10), QuestionBank1-4, ExamPaper1-9 |
| 2 | School2, Grade4-6, Class10-18, Student334-666, User(admin2,teacher11-20), QuestionBank5-8, ExamPaper10-18 |
| 3 | School3, Grade7-9, Class19-30, Student667-1000, User(admin3,teacher21-30), QuestionBank9-12, ExamPaper19-30 |

---

## 七、数据真实性保障

1. **题目内容**：按学科生成真实知识点题目
2. **学生姓名**：从中文姓名库随机组合
3. **成绩分布**：正态分布，平均分约75分
4. **时间戳**：使用2026年4-5月的合理日期时间

---

## 八、执行方式

生成的 SQL 脚本可与现有 init-data.sql 配合使用：
- 开发环境：可选择只执行 mock-data.sql
- 测试环境：可先执行 init-data.sql 再追加 mock-data.sql
- 数据库初始化时通过配置决定是否加载模拟数据