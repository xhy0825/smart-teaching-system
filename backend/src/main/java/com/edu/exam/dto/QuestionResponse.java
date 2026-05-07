package com.edu.exam.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目响应
 */
@Data
public class QuestionResponse {

    private Long id;

    private Long bankId;

    private String subject;

    private String questionType;

    private Integer difficulty;

    private String content;

    private String options;

    private String answer;

    private String answerAnalysis;

    private String knowledgePoints;

    private String source;

    private Long createdBy;

    private LocalDateTime createdAt;
}