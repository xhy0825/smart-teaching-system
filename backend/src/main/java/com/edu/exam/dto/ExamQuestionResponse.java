package com.edu.exam.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 试卷题目响应
 */
@Data
public class ExamQuestionResponse {

    private Long id;

    private Long examPaperId;

    private Long questionId;

    private Integer sequence;

    private BigDecimal score;

    private QuestionResponse question;
}