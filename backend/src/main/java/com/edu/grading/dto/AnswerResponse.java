package com.edu.grading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题详情响应
 */
@Data
public class AnswerResponse {

    private Long id;

    private Long answerSheetId;

    private Long examQuestionId;

    private String studentAnswer;

    private Integer isCorrect;

    private BigDecimal score;

    private BigDecimal aiScore;

    private BigDecimal manualScore;

    private String aiAnalysis;

    private LocalDateTime gradedAt;

    private Integer sequence;

    private String questionContent;
}