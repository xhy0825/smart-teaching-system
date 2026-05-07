package com.edu.grading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题详情实体
 */
@Data
@TableName("answer")
public class Answer {

    private Long id;

    private Long answerSheetId;

    private Long examQuestionId;

    private String studentAnswer;

    private Integer isCorrect;  // 0-错误, 1-正确, 2-部分正确

    private BigDecimal score;

    private BigDecimal aiScore;  // AI评分

    private BigDecimal manualScore;  // 人工评分

    private String aiAnalysis;  // AI批改分析

    private LocalDateTime gradedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}