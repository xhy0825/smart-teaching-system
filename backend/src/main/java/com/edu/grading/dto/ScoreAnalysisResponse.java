package com.edu.grading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩分析响应
 */
@Data
public class ScoreAnalysisResponse {

    private Long id;

    private Long examPaperId;

    private Long classId;

    private BigDecimal avgScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private BigDecimal passRate;

    private BigDecimal excellentRate;

    private List<QuestionAnalysisItem> questionAnalysis;

    private int studentCount;

    private int gradedCount;

    @Data
    public static class QuestionAnalysisItem {
        private Long questionId;
        private Integer sequence;
        private BigDecimal maxScore;
        private BigDecimal avgScore;
        private BigDecimal correctRate;
    }
}