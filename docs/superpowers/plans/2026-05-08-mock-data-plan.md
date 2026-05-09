# 模拟数据SQL脚本实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为教师智能教学系统生成包含1000条核心数据的模拟SQL脚本，用于测试和预览系统功能。

**Architecture:** 创建单一SQL脚本文件 `mock-data.sql`，按模块分组生成INSERT语句，数据遵循多租户隔离和业务关系约束。

**Tech Stack:** SQL (MySQL/H2兼容语法)

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `backend/src/main/resources/db/mock-data.sql` | 创建 | 包含所有模拟数据的 INSERT 语句 |

---

## Task 1: 生成租户和学校模块数据

**Files:**
- Create: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 创建SQL脚本文件头和租户数据**

写入租户模块的 INSERT 语句（3条租户记录）：

```sql
-- ============================================================
-- 模拟数据 - 用于测试和预览系统
-- 生成日期: 2026-05-08
-- ============================================================

-- ============================================================
-- 租户模块 (Tenant Module)
-- ============================================================

-- 创建3个租户
INSERT INTO tenant (id, name, code, ai_provider, status) VALUES
(1, '测试学校', 'TEST_SCHOOL', 'CLOUD', 1),
(2, '示范中学', 'DEMO_SCHOOL', 'CLOUD', 1),
(3, '实验学校', 'EXP_SCHOOL', 'PRIVATE', 1);
```

- [ ] **Step 2: 生成学校数据**

追加学校模块的 INSERT 语句（3条学校记录）：

```sql
-- ============================================================
-- 学校模块 (School Module)
-- ============================================================

-- 创建3所学校
INSERT INTO school (id, tenant_id, name, address) VALUES
(1, 1, '测试学校', '北京市海淀区测试路1号'),
(2, 2, '示范中学', '上海市浦东新区示范街2号'),
(3, 3, '实验学校', '广州市天河区实验大道3号');
```

- [ ] **Step 3: 验证SQL语法正确**

检查生成的SQL语句是否符合MySQL/H2语法规范。

---

## Task 2: 生成组织架构数据（年级、班级、学生）

**Files:**
- Modify: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 生成年级数据**

追加9条年级记录（每学校3个年级）：

```sql
-- ============================================================
-- 组织架构模块 (Organization Module)
-- ============================================================

-- 创建年级 (每学校3个年级: 七/八/九年级)
INSERT INTO grade (id, school_id, name, level, sequence) VALUES
-- 学校1
(1, 1, '七年级', 2, 1),
(2, 1, '八年级', 2, 2),
(3, 1, '九年级', 2, 3),
-- 学校2
(4, 2, '七年级', 2, 1),
(5, 2, '八年级', 2, 2),
(6, 2, '九年级', 2, 3),
-- 学校3
(7, 3, '七年级', 2, 1),
(8, 3, '八年级', 2, 2),
(9, 3, '九年级', 2, 3);
```

- [ ] **Step 2: 生成班级数据**

追加30条班级记录（每年级3个班级）：

```sql
-- 创建班级 (每年级3个班级)
INSERT INTO class (id, grade_id, name, student_count) VALUES
-- 七年级班级
(1, 1, '一班', 40),
(2, 1, '二班', 40),
(3, 1, '三班', 37),
-- 八年级班级 (学校1)
(4, 2, '一班', 40),
(5, 2, '二班', 40),
(6, 2, '三班', 40),
-- 九年级班级 (学校1)
(7, 3, '一班', 40),
(8, 3, '二班', 40),
(9, 3, '三班', 36),
-- 七年级班级 (学校2)
(10, 4, '一班', 40),
(11, 4, '二班', 40),
(12, 4, '三班', 40),
-- 八年级班级 (学校2)
(13, 5, '一班', 40),
(14, 5, '二班', 40),
(15, 5, '三班', 40),
-- 九年级班级 (学校2)
(16, 6, '一班', 40),
(17, 6, '二班', 40),
(18, 6, '三班', 40),
-- 七年级班级 (学校3)
(19, 7, '一班', 40),
(20, 7, '二班', 40),
(21, 7, '三班', 40),
-- 八年级班级 (学校3)
(22, 8, '一班', 40),
(23, 8, '二班', 40),
(24, 8, '三班', 40),
-- 九年级班级 (学校3)
(25, 9, '一班', 40),
(26, 9, '二班', 40),
(27, 9, '三班', 40),
-- 补充班级 (达到1000学生)
(28, 9, '四班', 40),
(29, 9, '五班', 40),
(30, 9, '六班', 43);
```

- [ ] **Step 3: 生成学生数据（1000条）**

生成1000条学生记录，使用中文姓名库和学号格式：

```sql
-- 创建学生 (1000条)
-- 学生姓名从中文姓名库随机组合，学号格式: 2024001-20241000
-- 按班级均匀分配: 班级1-9分配学生1-333(tenant_id=1)
-- 班级10-18分配学生334-666(tenant_id=2)
-- 班级19-30分配学生667-1000(tenant_id=3)
INSERT INTO student (id, tenant_id, class_id, name, student_no, gender, status, deleted) VALUES
-- 班级1的学生 (tenant_id=1, 共40人)
(1, 1, 1, '张明', '2024001', 1, 1, 0),
(2, 1, 1, '李华', '2024002', 1, 1, 0),
(3, 1, 1, '王芳', '2024003', 2, 1, 0),
... [完整1000条记录将在实现时生成]
```

**学生姓名生成规则：**
- 常见姓氏：张、李、王、刘、陈、杨、赵、黄、周、吴、徐、孙、胡、朱、高、林、何、郭、马、罗
- 常见名字（男）：明、华、强、伟、军、勇、杰、涛、磊、鹏、宇、浩、辉、波、峰
- 常见名字（女）：芳、娟、敏、静、丽、燕、艳、玲、婷、雪、梅、红、霞、琳、萍

---

## Task 3: 生成用户和权限模块数据

**Files:**
- Modify: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 生成角色数据（12条）**

追加角色记录（每租户4个角色）：

```sql
-- ============================================================
-- 用户模块 (User Module)
-- ============================================================

-- 创建角色 (每租户4个角色)
INSERT INTO role (id, tenant_id, name, code, description) VALUES
-- 租户1
(1, 1, '管理员', 'ADMIN', '系统管理员'),
(2, 1, '教师', 'TEACHER', '教师角色'),
(3, 1, '学生', 'STUDENT', '学生角色'),
(4, 1, '家长', 'PARENT', '家长角色'),
-- 租户2
(5, 2, '管理员', 'ADMIN', '系统管理员'),
(6, 2, '教师', 'TEACHER', '教师角色'),
(7, 2, '学生', 'STUDENT', '学生角色'),
(8, 2, '家长', 'PARENT', '家长角色'),
-- 租户3
(9, 3, '管理员', 'ADMIN', '系统管理员'),
(10, 3, '教师', 'TEACHER', '教师角色'),
(11, 3, '学生', 'STUDENT', '学生角色'),
(12, 3, '家长', 'PARENT', '家长角色');
```

- [ ] **Step 2: 复用权限数据**

保持现有 init-data.sql 的10条权限记录不变，在 mock-data.sql 中添加说明：

```sql
-- 权限数据保持不变，复用 init-data.sql 中的10条权限记录
-- INSERT INTO permission ... (已在 init-data.sql 中定义)
```

- [ ] **Step 3: 生成用户数据（50条）**

```sql
-- 创建用户 (50条)
-- 密码统一: BCrypt加密后的 'mock123'
-- 密码值: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e
INSERT INTO sys_user (id, tenant_id, username, password, real_name, status, deleted) VALUES
-- 管理员 (每租户1个)
(1, 1, 'admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '管理员1', 1, 0),
(2, 2, 'admin2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '管理员2', 1, 0),
(3, 3, 'admin3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '管理员3', 1, 0),
-- 教师 (租户1: teacher01-10, 租户2: teacher11-20, 租户3: teacher21-30)
(4, 1, 'teacher01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '张老师', 1, 0),
(5, 1, 'teacher02', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '李老师', 1, 0),
... [完整30条教师记录]
-- 学生账号 (关联前17个学生)
(34, 1, 'student001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '张明', 1, 0),
(35, 1, 'student002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJBo4lP3a2e', '李华', 1, 0),
... [完整17条学生账号记录]
```

- [ ] **Step 4: 生成用户角色关联（50条）**

```sql
-- 用户角色关联
INSERT INTO user_role (user_id, role_id) VALUES
-- 管理员关联ADMIN角色
(1, 1), (2, 5), (3, 9),
-- 教师关联TEACHER角色 (租户1: role_id=2, 租户2: role_id=6, 租户3: role_id=10)
(4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2), (13, 2),
(14, 6), (15, 6), (16, 6), (17, 6), (18, 6), (19, 6), (20, 6), (21, 6), (22, 6), (23, 6),
(24, 10), (25, 10), (26, 10), (27, 10), (28, 10), (29, 10), (30, 10), (31, 10), (32, 10), (33, 10),
-- 学生账号关联STUDENT角色 (租户1: role_id=3)
(34, 3), (35, 3), (36, 3), (37, 3), (38, 3), (39, 3), (40, 3), (41, 3), (42, 3), (43, 3),
(44, 3), (45, 3), (46, 3), (47, 3), (48, 3), (49, 3), (50, 3);
```

- [ ] **Step 5: 生成角色权限关联**

复用现有权限分配逻辑：

```sql
-- 角色权限关联 (复用init-data.sql的权限分配逻辑)
-- ADMIN角色拥有所有权限, TEACHER拥有教学相关权限, STUDENT拥有查看权限
INSERT INTO role_permission (role_id, permission_id) VALUES
-- 租户1 ADMIN角色 (所有权限)
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- 租户1 TEACHER角色
(2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10),
-- 租户1 STUDENT角色
(3, 7), (3, 9),
-- 租户2 ADMIN角色
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
-- 租户2 TEACHER角色
(6, 4), (6, 5), (6, 6), (6, 7), (6, 8), (6, 9), (6, 10),
-- 租户2 STUDENT角色
(7, 7), (7, 9),
-- 租户3 ADMIN角色
(9, 1), (9, 2), (9, 3), (9, 4), (9, 5), (9, 6), (9, 7), (9, 8), (9, 9), (9, 10),
-- 租户3 TEACHER角色
(10, 4), (10, 5), (10, 6), (10, 7), (10, 8), (10, 9), (10, 10),
-- 租户3 STUDENT角色
(11, 7), (11, 9);
```

---

## Task 4: 生成试卷模块数据（题库、题目）

**Files:**
- Modify: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 生成题库数据（12条）**

```sql
-- ============================================================
-- 试卷模块 (Exam Module)
-- ============================================================

-- 创建题库 (每租户4个: 数理化英)
INSERT INTO question_bank (id, tenant_id, name, subject, grade_level, description, is_public, created_by) VALUES
-- 租户1题库
(1, 1, '初中数学题库', 'MATH', 2, '初中数学练习题', 1, 1),
(2, 1, '初中物理题库', 'PHYSICS', 2, '初中物理练习题', 1, 1),
(3, 1, '初中化学题库', 'CHEMISTRY', 2, '初中化学练习题', 1, 1),
(4, 1, '初中英语题库', 'ENGLISH', 2, '初中英语练习题', 1, 1),
-- 租户2题库
(5, 2, '初中数学题库', 'MATH', 2, '初中数学练习题', 1, 2),
(6, 2, '初中物理题库', 'PHYSICS', 2, '初中物理练习题', 1, 2),
(7, 2, '初中化学题库', 'CHEMISTRY', 2, '初中化学练习题', 1, 2),
(8, 2, '初中英语题库', 'ENGLISH', 2, '初中英语练习题', 1, 2),
-- 租户3题库
(9, 3, '初中数学题库', 'MATH', 2, '初中数学练习题', 1, 3),
(10, 3, '初中物理题库', 'PHYSICS', 2, '初中物理练习题', 1, 3),
(11, 3, '初中化学题库', 'CHEMISTRY', 2, '初中化学练习题', 1, 3),
(12, 3, '初中英语题库', 'ENGLISH', 2, '初中英语练习题', 1, 3);
```

- [ ] **Step 2: 生成题目数据（1000条）**

按学科分布生成题目，数学300题、物理200题、化学200题、英语200题：

```sql
-- 创建题目 (1000条)
-- 数学题库: 题库1(100题), 题库5(100题), 题库9(100题)
-- 物理题库: 题库2(67题), 题库6(67题), 题库10(66题)
-- 化学题库: 题库3(67题), 题库7(67题), 题库11(66题)
-- 英语题库: 题库4(67题), 题库8(67题), 题库12(66题)
INSERT INTO question (id, bank_id, subject, question_type, difficulty, content, options, answer, answer_analysis, knowledge_points, source, created_by) VALUES
-- 数学选择题 (题库1, 简单难度)
(1, 1, 'MATH', 'CHOICE', 1, '下列哪个数是负数？', '{"A":"-1","B":"0","C":"1","D":"2"}', 'A', '负数小于0，-1是负数。', '["负数"]', 'MANUAL', 1),
(2, 1, 'MATH', 'CHOICE', 1, '计算: (-3)+5=', '{"A":"2","B":"-2","C":"8","D":"-8"}', 'A', '(-3)+5=2', '["有理数加减"]', 'MANUAL', 1),
(3, 1, 'MATH', 'CHOICE', 1, '绝对值最小的数是', '{"A":"-1","B":"0","C":"1","D":"-2"}', 'B', '0的绝对值是0，是最小的。', '["绝对值"]', 'MANUAL', 1),
... [完整1000条题目记录将在实现时生成]

-- 题目内容生成规则：
-- 选择题(CHOICE): 4个选项A/B/C/D，difficulty=1/2/3
-- 填空题(FILL): options=NULL，answer为填空答案
-- 判断题(JUDGE): options=NULL，answer为"正确"或"错误"
-- 计算题(CALCULATION): options=NULL，answer为计算结果和步骤
```

**题目学科分布详情：**

| 学科 | 题库ID | 题目ID范围 | 数量 |
|------|--------|-----------|------|
| MATH | 1 | 1-100 | 100 |
| MATH | 5 | 301-400 | 100 |
| MATH | 9 | 601-700 | 100 |
| PHYSICS | 2 | 101-167 | 67 |
| PHYSICS | 6 | 401-467 | 67 |
| PHYSICS | 10 | 701-766 | 66 |
| CHEMISTRY | 3 | 168-234 | 67 |
| CHEMISTRY | 7 | 468-534 | 67 |
| CHEMISTRY | 11 | 767-832 | 66 |
| ENGLISH | 4 | 235-301 | 67 |
| ENGLISH | 8 | 535-601 | 67 |
| ENGLISH | 12 | 833-900 | 67 |

**题型分布（每题库）：**
- 选择题(CHOICE): 50%
- 填空题(FILL): 30%
- 判断题(JUDGE): 10%
- 计算题(CALCULATION): 10%

---

## Task 5: 生成试卷模板和试卷数据

**Files:**
- Modify: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 生成试卷模板数据（12条）**

```sql
-- 创建试卷模板 (每租户4个)
INSERT INTO exam_template (id, tenant_id, name, subject, total_score, time_limit, structure, created_by) VALUES
-- 租户1模板
(1, 1, '初中数学单元测试模板', 'MATH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 1),
(2, 1, '初中物理单元测试模板', 'PHYSICS', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 1),
(3, 1, '初中化学单元测试模板', 'CHEMISTRY', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 1),
(4, 1, '初中英语单元测试模板', 'ENGLISH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"简答题","type":"CALCULATION","count":3,"scoreEach":12}]', 1),
-- 租户2模板
(5, 2, '初中数学单元测试模板', 'MATH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 2),
(6, 2, '初中物理单元测试模板', 'PHYSICS', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 2),
(7, 2, '初中化学单元测试模板', 'CHEMISTRY', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 2),
(8, 2, '初中英语单元测试模板', 'ENGLISH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"简答题","type":"CALCULATION","count":3,"scoreEach":12}]', 2),
-- 租户3模板
(9, 3, '初中数学单元测试模板', 'MATH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 3),
(10, 3, '初中物理单元测试模板', 'PHYSICS', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 3),
(11, 3, '初中化学单元测试模板', 'CHEMISTRY', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 3),
(12, 3, '初中英语单元测试模板', 'ENGLISH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"简答题","type":"CALCULATION","count":3,"scoreEach":12}]', 3);
```

- [ ] **Step 2: 生成试卷数据（30条）**

```sql
-- 创建试卷 (30条，每班级1张已批改试卷)
-- 状态: 4-已批改
INSERT INTO exam_paper (id, tenant_id, template_id, title, subject, grade_id, class_id, total_score, time_limit, status, created_by, published_at) VALUES
-- 租户1试卷 (班级1-9对应)
(1, 1, 1, '七年级一班数学测试', 'MATH', 1, 1, 100, 45, 4, 4, '2026-04-15 08:00:00'),
(2, 1, 1, '七年级二班数学测试', 'MATH', 1, 2, 100, 45, 4, 4, '2026-04-15 08:00:00'),
(3, 1, 1, '七年级三班数学测试', 'MATH', 1, 3, 100, 45, 4, 4, '2026-04-15 08:00:00'),
(4, 1, 1, '八年级一班数学测试', 'MATH', 2, 4, 100, 45, 4, 5, '2026-04-16 08:00:00'),
... [完整30条试卷记录]
```

- [ ] **Step 3: 生成试卷题目关联（900条）**

```sql
-- 创建试卷题目关联 (每试卷30题)
INSERT INTO exam_question (id, exam_paper_id, question_id, sequence, score) VALUES
-- 试卷1的题目 (从题库1选取，sequence 1-30)
(1, 1, 1, 1, 4),
(2, 1, 2, 2, 4),
(3, 1, 3, 3, 4),
... [完整900条记录]
```

---

## Task 6: 生成批改模块数据（答题卡、答案、成绩分析）

**Files:**
- Modify: `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 生成答题卡数据（1000条）**

```sql
-- ============================================================
-- 批改模块 (Grading Module)
-- ============================================================

-- 创建答题卡 (1000条，每学生1张)
-- 分数随机生成: 60-100分，正态分布偏向80分
-- 状态: 3-已批改
INSERT INTO answer_sheet (id, tenant_id, exam_paper_id, student_id, status, total_score, submit_time, grading_time, graded_by, deleted) VALUES
-- 班级1学生的答题卡 (exam_paper_id=1)
(1, 1, 1, 1, 3, 85, '2026-04-15 09:00:00', '2026-04-15 10:00:00', 4, 0),
(2, 1, 1, 2, 3, 72, '2026-04-15 09:00:00', '2026-04-15 10:00:00', 4, 0),
(3, 1, 1, 3, 3, 91, '2026-04-15 09:00:00', '2026-04-15 10:00:00', 4, 0),
... [完整1000条记录]
```

**分数生成规则：**
- 使用正态分布，平均值80，标准差10
- 分数范围: 60-100
- 及格率约85%，优秀率约40%

- [ ] **Step 2: 生成答案数据（约30000条）**

```sql
-- 创建答案详情 (每答题卡30个答案)
-- 正确率约70%，is_correct随机分配
INSERT INTO answer (id, answer_sheet_id, exam_question_id, student_answer, is_correct, score, ai_score, ai_analysis, graded_at) VALUES
-- 答题卡1的答案
(1, 1, 1, 'A', 1, 4, 4, '答案正确', '2026-04-15 10:00:00'),
(2, 1, 2, 'A', 1, 4, 4, '答案正确', '2026-04-15 10:00:00'),
(3, 1, 3, 'B', 0, 0, 0, '答案错误，正确答案是A', '2026-04-15 10:00:00'),
... [完整30000条记录将在实现时批量生成]
```

- [ ] **Step 3: 生成成绩分析数据（30条）**

```sql
-- 创建成绩分析 (每班级每试卷1条)
-- 根据该班级学生答题卡计算统计数据
INSERT INTO score_analysis (id, exam_paper_id, class_id, avg_score, max_score, min_score, pass_rate, excellent_rate, question_analysis) VALUES
-- 班级1的成绩分析 (试卷1)
(1, 1, 1, 78.5, 98, 62, 0.875, 0.35, '{"q1":{"avg":3.8,"correct_rate":0.95},"q2":{"avg":3.2,"correct_rate":0.80}}'),
-- 班级2的成绩分析 (试卷2)
(2, 2, 2, 75.2, 95, 58, 0.85, 0.30, '{"q1":{"avg":3.5,"correct_rate":0.88}}'),
... [完整30条记录]
```

- [ ] **Step 4: 生成学生错题数据（约500条）**

```sql
-- 创建学生错题记录 (从答案中筛选is_correct=0的记录)
INSERT INTO student_wrong_question (id, student_id, question_id, exam_paper_id, wrong_count, last_wrong_at) VALUES
-- 学生1的错题
(1, 1, 3, 1, 1, '2026-04-15 10:00:00'),
(2, 1, 5, 1, 2, '2026-04-15 10:00:00'),
... [完整500条记录]
```

---

## Task 7: 验证和提交

**Files:**
- `backend/src/main/resources/db/mock-data.sql`

- [ ] **Step 1: 检查SQL脚本完整性**

验证所有INSERT语句：
- 语法正确（MySQL/H2兼容）
- 外键引用正确
- 数据量符合设计要求

- [ ] **Step 2: 检查数据关系一致性**

验证：
- 所有 tenant_id 引用存在
- 所有 class_id、grade_id、school_id 关联正确
- 学生班级分配均匀

- [ ] **Step 3: 提交SQL脚本**

```bash
git add backend/src/main/resources/db/mock-data.sql
git commit -m "feat: add mock data SQL script with 1000+ records for testing"
```

---

## 自检清单

1. **规格覆盖**：
   - Tenant(3条) ✓ Task 1
   - School(3条) ✓ Task 1
   - Grade(9条) ✓ Task 2
   - Class(30条) ✓ Task 2
   - Student(1000条) ✓ Task 2
   - Role(12条) ✓ Task 3
   - User(50条) ✓ Task 3
   - UserRole(50条) ✓ Task 3
   - QuestionBank(12条) ✓ Task 4
   - Question(1000条) ✓ Task 4
   - ExamTemplate(12条) ✓ Task 5
   - ExamPaper(30条) ✓ Task 5
   - AnswerSheet(1000条) ✓ Task 6
   - ScoreAnalysis(30条) ✓ Task 6

2. **占位符检查**：
   - 所有 `[完整XXX条记录将在实现时生成]` 说明需要实际生成数据
   - 学生姓名、题目内容需要按规则生成

3. **类型一致性**：
   - 所有ID使用BIGINT
   - 所有tenant_id正确引用tenant表
   - 所有status字段使用正确的数值