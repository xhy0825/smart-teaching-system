package com.edu.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 试卷创建请求
 */
@Data
public class ExamPaperCreateRequest {

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    @NotBlank(message = "学科不能为空")
    private String subject;

    private Long templateId;

    private Long gradeId;

    private Long classId;

    @NotNull(message = "总分不能为空")
    private BigDecimal totalScore;

    private Integer timeLimit;

    private Long createdBy;
}