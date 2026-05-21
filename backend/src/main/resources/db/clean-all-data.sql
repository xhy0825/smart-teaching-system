-- ============================================================
-- 一键清除所有数据脚本
-- 执行前请确认已备份重要数据！
-- 按外键依赖逆序清除（子表 → 父表）
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- 批改模块
DELETE FROM answer;
DELETE FROM answer_sheet;
DELETE FROM student_wrong_question;
DELETE FROM score_analysis;

-- 试卷模块
DELETE FROM exam_question;
DELETE FROM exam_paper;
DELETE FROM exam_template;
DELETE FROM question;
DELETE FROM question_bank;

-- 组织架构模块
DELETE FROM student;
DELETE FROM class;
DELETE FROM grade;
DELETE FROM school;

-- 用户模块
DELETE FROM user_role;
DELETE FROM role_permission;
DELETE FROM sys_user;
DELETE FROM role;
DELETE FROM permission;

-- 租户模块
DELETE FROM tenant_config;
DELETE FROM tenant;

-- PPT模块
DELETE FROM ppt_document;

-- 重置自增ID
ALTER TABLE answer AUTO_INCREMENT = 1;
ALTER TABLE answer_sheet AUTO_INCREMENT = 1;
ALTER TABLE student_wrong_question AUTO_INCREMENT = 1;
ALTER TABLE score_analysis AUTO_INCREMENT = 1;
ALTER TABLE exam_question AUTO_INCREMENT = 1;
ALTER TABLE exam_paper AUTO_INCREMENT = 1;
ALTER TABLE exam_template AUTO_INCREMENT = 1;
ALTER TABLE question AUTO_INCREMENT = 1;
ALTER TABLE question_bank AUTO_INCREMENT = 1;
ALTER TABLE student AUTO_INCREMENT = 1;
ALTER TABLE class AUTO_INCREMENT = 1;
ALTER TABLE grade AUTO_INCREMENT = 1;
ALTER TABLE school AUTO_INCREMENT = 1;
ALTER TABLE sys_user AUTO_INCREMENT = 1;
ALTER TABLE role AUTO_INCREMENT = 1;
ALTER TABLE permission AUTO_INCREMENT = 1;
ALTER TABLE user_role AUTO_INCREMENT = 1;
ALTER TABLE role_permission AUTO_INCREMENT = 1;
ALTER TABLE tenant_config AUTO_INCREMENT = 1;
ALTER TABLE tenant AUTO_INCREMENT = 1;
ALTER TABLE ppt_document AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All data cleaned successfully!' AS result;
