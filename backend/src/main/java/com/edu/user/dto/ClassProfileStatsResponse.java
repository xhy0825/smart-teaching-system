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

    /**
     * 知识点雷达图数据
     */
    @Data
    public static class KnowledgeRadar {
        private List<String> points;      // 知识点名称列表
        private List<BigDecimal> scores;  // 对应掌握率（0-100）
    }

    /**
     * 成绩箱线图数据
     */
    @Data
    public static class ScoreBoxplot {
        private BigDecimal min;
        private BigDecimal q1;
        private BigDecimal median;
        private BigDecimal q3;
        private BigDecimal max;
        private List<BigDecimal> outliers;  // 异常值
    }

    // 图表数据字段
    private KnowledgeRadar knowledgeRadar;
    private ScoreBoxplot scoreBoxplot;
}
