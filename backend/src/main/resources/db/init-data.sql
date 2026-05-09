-- ============================================================
-- 初始化数据 - 创建默认租户和用户
-- ============================================================

-- 创建默认租户
INSERT INTO tenant (id, name, code, ai_provider, status) VALUES
(1, '测试学校', 'TEST_SCHOOL', 'CLOUD', 1);

-- 创建默认学校
INSERT INTO school (id, tenant_id, name, address) VALUES
(1, 1, '测试学校', '测试地址');

-- 创建默认年级
INSERT INTO grade (id, school_id, name, level, sequence) VALUES
(1, 1, '七年级', 2, 1),
(2, 1, '八年级', 2, 2),
(3, 1, '九年级', 2, 3);

-- 创建默认班级
INSERT INTO class (id, grade_id, name, student_count) VALUES
(1, 1, '一班', 0),
(2, 1, '二班', 0);

-- 创建默认角色
INSERT INTO role (id, tenant_id, name, code, description) VALUES
(1, 1, '管理员', 'ADMIN', '系统管理员'),
(2, 1, '教师', 'TEACHER', '教师角色'),
(3, 1, '学生', 'STUDENT', '学生角色');

-- 创建默认权限
INSERT INTO permission (id, code, name, resource, action) VALUES
(1, 'tenant:manage', '租户管理', 'tenant', 'manage'),
(2, 'user:create', '创建用户', 'user', 'create'),
(3, 'class:manage', '班级管理', 'class', 'manage'),
(4, 'question:create', '创建题目', 'question', 'create'),
(5, 'exam:create', '创建试卷', 'exam', 'create'),
(6, 'exam:publish', '发布试卷', 'exam', 'publish'),
(7, 'grading:view', '查看批改', 'grading', 'view'),
(8, 'grading:review', '批改复核', 'grading', 'review'),
(9, 'score:view', '查看成绩', 'score', 'view'),
(10, 'score:export', '导出成绩', 'score', 'export');

-- 角色权限关联
INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10),
(3, 7), (3, 9);

-- 创建默认管理员用户 (密码: admin123)
-- BCrypt加密后的密码
INSERT INTO sys_user (id, tenant_id, username, password, real_name, status, deleted) VALUES
(1, 1, 'admin', '$2a$10$1VqglJ1Xi1mYSOuGBd7ldePdpdJXlEqBOiv3mk.Sef7Niy8Fi0LUO', '管理员', 1, 0);

-- 用户角色关联
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1);

-- 创建默认题库
INSERT INTO question_bank (id, tenant_id, name, subject, grade_level, description, is_public, created_by) VALUES
(1, 1, '初中数学题库', 'MATH', 2, '初中数学练习题', 1, 1),
(2, 1, '初中物理题库', 'PHYSICS', 2, '初中物理练习题', 1, 1),
(3, 1, '初中化学题库', 'CHEMISTRY', 2, '初中化学练习题', 1, 1),
(4, 1, '初中英语题库', 'ENGLISH', 2, '初中英语练习题', 1, 1);

-- 创建示例题目
INSERT INTO question (id, bank_id, subject, question_type, difficulty, content, options, answer, answer_analysis, knowledge_points, source, created_by) VALUES
(1, 1, 'MATH', 'CHOICE', 1, '下列哪个数是负数？', '{"A":"-1","B":"0","C":"1","D":"2"}', 'A', '负数小于0，-1是负数。', '["负数"]', 'MANUAL', 1),
(2, 1, 'MATH', 'CHOICE', 2, '计算: 2×3+4=', '{"A":"8","B":"10","C":"12","D":"14"}', 'B', '先算乘法: 2×3=6，再加4得10。', '["乘法","加法"]', 'MANUAL', 1),
(3, 1, 'MATH', 'FILL', 1, '圆的周长公式是C=____', NULL, '2πr或πd', '圆的周长等于直径乘π。', '["圆的周长"]', 'MANUAL', 1),
(4, 1, 'MATH', 'JUDGE', 1, '三角形的内角和是180度。', NULL, '正确', '三角形内角和定理。', '["三角形"]', 'MANUAL', 1),
(5, 1, 'MATH', 'CALCULATION', 2, '解方程: x + 5 = 12', NULL, 'x = 7', '移项得 x = 12 - 5 = 7', '["方程"]', 'MANUAL', 1);

-- 创建试卷模板
INSERT INTO exam_template (id, tenant_id, name, subject, total_score, time_limit, structure, created_by) VALUES
(1, 1, '初中数学单元测试模板', 'MATH', 100, 45, '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4},{"section":"填空题","type":"FILL","count":5,"scoreEach":4},{"section":"计算题","type":"CALCULATION","count":3,"scoreEach":12}]', 1);

-- 创建测试学生数据
INSERT INTO student (id, tenant_id, class_id, name, student_no, gender, status, deleted) VALUES
(1, 1, 1, '张三', '2023001', 1, 1, 0),
(2, 1, 1, '李四', '2023002', 1, 1, 0),
(3, 1, 1, '王五', '2023003', 2, 1, 0),
(4, 1, 2, '赵六', '2023004', 1, 1, 0);

-- 创建学生错题记录（用于个性化出题测试）
INSERT INTO student_wrong_question (student_id, question_id, wrong_count) VALUES
(1, 2, 2),
(1, 5, 1),
(2, 1, 1),
(2, 3, 1);