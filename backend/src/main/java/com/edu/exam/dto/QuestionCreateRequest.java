package com.edu.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题目创建请求
 */
@Data
public class QuestionCreateRequest {

    @NotNull(message = "题库ID不能为空")
    private Long bankId;

    @NotBlank(message = "学科不能为空")
    private String subject;

    @NotBlank(message = "题型不能为空")
    private String questionType;

    private Integer difficulty;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String answerAnalysis;

    private String knowledgePoints;

    private Long createdBy;
}