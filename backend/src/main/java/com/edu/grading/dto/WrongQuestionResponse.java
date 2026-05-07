package com.edu.grading.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题记录响应
 */
@Data
public class WrongQuestionResponse {

    private Long id;

    private Long studentId;

    private Long questionId;

    private Long examPaperId;

    private Integer wrongCount;

    private LocalDateTime lastWrongAt;

    private LocalDateTime correctedAt;

    private String questionContent;

    private String subject;

    private String questionType;
}