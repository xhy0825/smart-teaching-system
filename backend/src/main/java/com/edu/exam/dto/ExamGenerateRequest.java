package com.edu.exam.dto;

import lombok.Data;

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
}
