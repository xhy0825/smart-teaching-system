package com.edu.grading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩分析实体
 */
@Data
@TableName("score_analysis")
public class ScoreAnalysis {

    private Long id;

    private Long examPaperId;

    private Long classId;

    private BigDecimal avgScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private BigDecimal passRate;  // 及格率

    private BigDecimal excellentRate;  // 优秀率

    private String questionAnalysis;  // JSON格式的题目分析

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}