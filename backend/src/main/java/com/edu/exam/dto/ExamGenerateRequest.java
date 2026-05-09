package com.edu.exam.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExamGenerateRequest {
    private String title;
    private String subject;
    private Long gradeId;
    private Long classId;
    private Double totalScore;
    private Integer timeLimit;
    private String structure;  // JSON格式试卷结构
    private Long createdBy;

    // 新增：难度分布配置 {"1": 0.3, "2": 0.5, "3": 0.2} - key为字符串
    private Map<String, Double> difficultyDistribution;

    // 新增：知识点覆盖列表
    private String[] knowledgePoints;

    // 新增：生成策略 SIMPLE/SMART/AI
    private String generateStrategy;

    // 新增：目标学生ID（个性化出题）
    private Long targetStudentId;
}
