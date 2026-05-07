package com.edu.grading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题卡响应
 */
@Data
public class AnswerSheetResponse {

    private Long id;

    private Long tenantId;

    private Long examPaperId;

    private Long studentId;

    private Integer status;

    private BigDecimal totalScore;

    private LocalDateTime submitTime;

    private LocalDateTime gradingTime;

    private Long gradedBy;

    private LocalDateTime createdAt;

    private String studentName;
}