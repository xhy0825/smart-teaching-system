package com.edu.user.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ClassProfileStatsResponse {

    private BaseStats baseStats;
    private List<ScoreRange> distribution;
    private List<KnowledgeMastery> knowledgeMastery;

    @Data
    public static class BaseStats {
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        private int studentCount;
        private BigDecimal passRate;
        private BigDecimal excellentRate;
        private int gradedCount;
    }

    @Data
    public static class ScoreRange {
        private String range;
        private int count;
    }

    @Data
    public static class KnowledgeMastery {
        private String knowledgePoint;
        private BigDecimal avgMasteryRate;
        private int weakStudentCount;
        private String level;
    }
}
