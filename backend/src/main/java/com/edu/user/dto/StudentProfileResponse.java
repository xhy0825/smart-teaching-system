package com.edu.user.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学生画像响应DTO
 */
@Data
public class StudentProfileResponse {
    // 基本信息
    private Long id;
    private String name;
    private String studentNo;
    private Long classId;
    private String className;
    private String gradeName;
    private Integer gender;
    private LocalDate birthDate;
    private Integer age;

    // 成绩统计
    private Integer examCount;          // 参加考试次数
    private BigDecimal avgScore;        // 平均分
    private BigDecimal highestScore;    // 最高分
    private BigDecimal lowestScore;     // 最低分
    private BigDecimal passRate;        // 及格率
    private BigDecimal excellentRate;   // 优秀率
    private Integer ranking;            // 班级排名
    private Integer totalStudents;      // 班级总人数

    // 知识点掌握情况
    private List<KnowledgePointStats> knowledgePoints;

    // 学科成绩分析
    private List<SubjectScore> subjectScores;

    // 错题统计
    private Integer totalWrongCount;    // 总错题数
    private Integer correctedCount;     // 已纠错数
    private List<WrongQuestionType> wrongQuestionTypes;  // 错题类型分布

    // 学习趋势（最近几次考试）
    private List<ScoreTrend> scoreTrends;

    // 特长爱好（预留）
    private String interests;
    private String talents;
    private String learningStyle;

    /**
     * 知识点掌握统计
     */
    @Data
    public static class KnowledgePointStats {
        private String knowledgePoint;
        private BigDecimal masteryRate;   // 掌握率
        private Integer correctCount;     // 正确次数
        private Integer totalCount;       // 总次数
        private String level;             // 掌握等级：优秀/良好/一般/薄弱
    }

    /**
     * 学科成绩
     */
    @Data
    public static class SubjectScore {
        private String subject;
        private String subjectName;
        private BigDecimal avgScore;
        private BigDecimal highestScore;
        private BigDecimal lowestScore;
        private Integer examCount;
        private String level;             // 等级：优秀/良好/及格/不及格
    }

    /**
     * 错题类型
     */
    @Data
    public static class WrongQuestionType {
        private String questionType;
        private String typeName;
        private Integer count;
        private BigDecimal percentage;
    }

    /**
     * 成绩趋势
     */
    @Data
    public static class ScoreTrend {
        private Long examPaperId;
        private String examName;
        private LocalDate examDate;
        private BigDecimal score;
        private BigDecimal avgClassScore;  // 班级平均分
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
     * 错题类型饼图数据
     */
    @Data
    public static class WrongTypePie {
        private List<String> types;        // 题型列表
        private List<Integer> counts;      // 对应错题数量
    }

    // 图表数据字段
    private KnowledgeRadar knowledgeRadar;
    private WrongTypePie wrongTypePie;
}