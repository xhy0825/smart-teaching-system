package com.edu.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 试卷模板创建请求
 */
@Data
public class ExamTemplateCreateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotBlank(message = "学科不能为空")
    private String subject;

    private BigDecimal totalScore;

    private Integer timeLimit;

    private String structure;

    private Long createdBy;
}