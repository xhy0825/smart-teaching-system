package com.edu.exam.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷模板响应
 */
@Data
public class ExamTemplateResponse {

    private Long id;

    private Long tenantId;

    private String name;

    private String subject;

    private BigDecimal totalScore;

    private Integer timeLimit;

    private String structure;

    private Long createdBy;

    private LocalDateTime createdAt;
}