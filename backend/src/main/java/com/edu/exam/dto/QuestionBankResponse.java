package com.edu.exam.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库响应
 */
@Data
public class QuestionBankResponse {

    private Long id;

    private Long tenantId;

    private String name;

    private String subject;

    private Integer gradeLevel;

    private String description;

    private Integer isPublic;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Integer questionCount;
}