package com.edu.exam.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷响应
 */
@Data
public class ExamPaperResponse {

    private Long id;

    private Long tenantId;

    private Long templateId;

    private String title;

    private String subject;

    private Long gradeId;

    private Long classId;

    private BigDecimal totalScore;

    private Integer timeLimit;

    private Integer status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private List<ExamQuestionResponse> questions;
}