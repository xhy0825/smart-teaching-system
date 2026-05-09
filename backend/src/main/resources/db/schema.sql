-- ============================================================
-- Teacher AI Platform MVP - Database Schema
-- ============================================================

-- ============================================================
-- 租户模块表 (Tenant Module)
-- ============================================================

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

-- ============================================================
-- 用户模块表 (User Module)
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(200) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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

-- ============================================================
-- 组织架构表 (Organization)
-- ============================================================

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
    teacher_id BIGINT COMMENT '班主任ID（关联sys_user）',
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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_student_no (tenant_id, student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ============================================================
-- 试卷模块表 (Exam Module)
-- ============================================================

-- 题库表
CREATE TABLE IF NOT EXISTS question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题库ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    name VARCHAR(100) NOT NULL COMMENT '题库名称',
    subject VARCHAR(20) NOT NULL COMMENT '学科: MATH/PHYSICS/CHEMISTRY/ENGLISH',
    grade_level TINYINT COMMENT '适用年级段',
    description VARCHAR(200) COMMENT '题库描述',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-私有, 1-租户内公开',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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

-- ============================================================
-- 批改模块表 (Grading Module)
-- ============================================================

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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
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

-- ============================================================
-- Indexes on FK-like columns
-- ============================================================

-- tenant_config
CREATE INDEX idx_tenant_config_tenant_id ON tenant_config(tenant_id);

-- school
CREATE INDEX idx_school_tenant_id ON school(tenant_id);

-- sys_user
CREATE INDEX idx_sys_user_tenant_id ON sys_user(tenant_id);

-- role
CREATE INDEX idx_role_tenant_id ON role(tenant_id);

-- grade
CREATE INDEX idx_grade_school_id ON grade(school_id);

-- class
CREATE INDEX idx_class_grade_id ON class(grade_id);
CREATE INDEX idx_class_teacher_id ON class(teacher_id);

-- student
CREATE INDEX idx_student_class_id ON student(class_id);
CREATE INDEX idx_student_tenant_id ON student(tenant_id);

-- question
CREATE INDEX idx_question_bank_id ON question(bank_id);

-- exam_template
CREATE INDEX idx_exam_template_tenant_id ON exam_template(tenant_id);

-- exam_paper
CREATE INDEX idx_exam_paper_tenant_id ON exam_paper(tenant_id);
CREATE INDEX idx_exam_paper_template_id ON exam_paper(template_id);

-- exam_question
CREATE INDEX idx_exam_question_exam_paper_id ON exam_question(exam_paper_id);
CREATE INDEX idx_exam_question_question_id ON exam_question(question_id);

-- answer_sheet
CREATE INDEX idx_answer_sheet_tenant_id ON answer_sheet(tenant_id);
CREATE INDEX idx_answer_sheet_exam_paper_id ON answer_sheet(exam_paper_id);
CREATE INDEX idx_answer_sheet_student_id ON answer_sheet(student_id);

-- answer
CREATE INDEX idx_answer_answer_sheet_id ON answer(answer_sheet_id);
CREATE INDEX idx_answer_exam_question_id ON answer(exam_question_id);

-- score_analysis
CREATE INDEX idx_score_analysis_exam_paper_id ON score_analysis(exam_paper_id);
CREATE INDEX idx_score_analysis_class_id ON score_analysis(class_id);

-- student_wrong_question
CREATE INDEX idx_student_wrong_question_student_id ON student_wrong_question(student_id);
CREATE INDEX idx_student_wrong_question_question_id ON student_wrong_question(question_id);

-- ============================================================
-- PPT模块表 (PPT Module)
-- ============================================================

-- PPT文档表
CREATE TABLE IF NOT EXISTS ppt_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PPT ID',
    tenant_id BIGINT NOT NULL COMMENT '所属租户',
    title VARCHAR(100) NOT NULL COMMENT 'PPT标题',
    subject VARCHAR(20) COMMENT '学科',
    template_type VARCHAR(20) COMMENT '模板类型: LESSON/EXAM/SUMMARY',
    content_json TEXT COMMENT '内容JSON',
    file_path VARCHAR(200) COMMENT '文件路径',
    page_count INT COMMENT '页数',
    created_by BIGINT COMMENT '创建人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PPT文档表';

-- ppt_document indexes
CREATE INDEX idx_ppt_document_tenant_id ON ppt_document(tenant_id);
