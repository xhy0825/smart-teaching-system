package com.edu.user.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import com.edu.user.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j

@Service
@RequiredArgsConstructor
public class ClassProfileService {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService studentWrongQuestionService;
    private final com.edu.user.service.StudentProfileService studentProfileService;
    private final com.edu.user.service.StudentService studentService;
    private final QuestionService questionService;
    private final AnswerSheetMapper answerSheetMapper;

    public ClassProfileStatsResponse getClassStats(Long classId) {
        log.info("开始获取班级画像: classId={}", classId);
        ClassProfileStatsResponse response = new ClassProfileStatsResponse();

        try {
            // 1. 获取成绩分析数据（从 score_analysis 表）
            log.debug("查询成绩分析: classId={}", classId);
            ScoreAnalysis latestAnalysis = scoreAnalysisService.getLatestByClassId(classId);
            log.debug("成绩分析结果: {}", latestAnalysis);

            ClassProfileStatsResponse.BaseStats baseStats = new ClassProfileStatsResponse.BaseStats();
            if (latestAnalysis != null) {
                baseStats.setAvgScore(latestAnalysis.getAvgScore());
                baseStats.setMaxScore(latestAnalysis.getMaxScore());
                baseStats.setMinScore(latestAnalysis.getMinScore());
                baseStats.setPassRate(latestAnalysis.getPassRate());
                baseStats.setExcellentRate(latestAnalysis.getExcellentRate());
                baseStats.setStudentCount(studentService.listByClass(classId).size());
                baseStats.setGradedCount(0);
            } else {
                baseStats.setAvgScore(BigDecimal.ZERO);
                baseStats.setMaxScore(BigDecimal.ZERO);
                baseStats.setMinScore(BigDecimal.ZERO);
                baseStats.setPassRate(BigDecimal.ZERO);
                baseStats.setExcellentRate(BigDecimal.ZERO);
                baseStats.setStudentCount(studentService.listByClass(classId).size());
                baseStats.setGradedCount(0);
            }
            response.setBaseStats(baseStats);

            // 2. 分数段分布（暂时返回空列表，后续扩展）
            response.setDistribution(new ArrayList<>());

            // 3. 知识点掌握分布
            log.debug("开始构建知识点掌握分布: classId={}", classId);
            response.setKnowledgeMastery(buildKnowledgeMastery(classId));

            // 4. 知识点雷达图
            response.setKnowledgeRadar(buildKnowledgeRadar(classId));

            // 5. 成绩箱线图
            response.setScoreBoxplot(buildScoreBoxplot(classId));

            log.info("班级画像获取完成: classId={}", classId);
        } catch (Exception e) {
            log.error("获取班级画像失败: classId=" + classId, e);
            throw e;
        }

        return response;
    }

    private List<ClassProfileStatsResponse.KnowledgeMastery> buildKnowledgeMastery(Long classId) {
        List<ClassProfileStatsResponse.KnowledgeMastery> result = new ArrayList<>();
        List<Student> students = studentService.listByClass(classId);
        if (students.isEmpty()) {
            return result;
        }

        Map<String, KnowledgePointAgg> aggMap = new HashMap<>();
        for (Student student : students) {
            List<StudentWrongQuestion> wrongQuestions = studentWrongQuestionService.listByStudent(student.getId());
            for (StudentWrongQuestion wq : wrongQuestions) {
                Question question = questionService.getQuestionById(wq.getQuestionId());
                if (question != null && question.getKnowledgePoints() != null) {
                    try {
                        JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                        if (points != null) {
                            for (int i = 0; i < points.size(); i++) {
                                String point = points.getString(i);
                                aggMap.computeIfAbsent(point, k -> new KnowledgePointAgg()).addStudent(student.getId());
                            }
                        }
                    } catch (Exception e) {
                        // 解析失败，跳过
                    }
                }
            }
        }

        for (Map.Entry<String, KnowledgePointAgg> entry : aggMap.entrySet()) {
            ClassProfileStatsResponse.KnowledgeMastery km = new ClassProfileStatsResponse.KnowledgeMastery();
            km.setKnowledgePoint(entry.getKey());
            double masteryRate = 1.0 - (entry.getValue().getWrongCount() / (double) students.size());
            km.setAvgMasteryRate(BigDecimal.valueOf(Math.max(0, masteryRate * 100)).setScale(2, RoundingMode.HALF_UP));
            km.setWeakStudentCount(entry.getValue().getWeakStudentCount());
            km.setLevel(determineLevel(km.getAvgMasteryRate()));
            result.add(km);
        }

        return result;
    }

    private String determineLevel(BigDecimal rate) {
        if (rate.compareTo(new BigDecimal("90")) >= 0) return "优秀";
        if (rate.compareTo(new BigDecimal("75")) >= 0) return "良好";
        if (rate.compareTo(new BigDecimal("60")) >= 0) return "一般";
        return "薄弱";
    }

    /**
     * 构建班级知识点雷达图数据
     */
    private ClassProfileStatsResponse.KnowledgeRadar buildKnowledgeRadar(Long classId) {
        List<Student> students = studentService.listByClass(classId);
        if (students.isEmpty()) {
            return new ClassProfileStatsResponse.KnowledgeRadar();
        }

        // 按知识点聚合班级整体掌握情况
        Map<String, KnowledgePointClassAgg> aggMap = new HashMap<>();

        for (Student student : students) {
            List<StudentWrongQuestion> wrongQuestions = studentWrongQuestionService.listByStudent(student.getId());
            for (StudentWrongQuestion wq : wrongQuestions) {
                Question question = questionService.getQuestionById(wq.getQuestionId());
                if (question != null && question.getKnowledgePoints() != null) {
                    try {
                        JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                        if (points != null) {
                            for (int i = 0; i < points.size(); i++) {
                                String point = points.getString(i);
                                aggMap.computeIfAbsent(point, k -> new KnowledgePointClassAgg())
                                       .addStudent(student.getId());
                            }
                        }
                    } catch (Exception e) {
                        // 解析失败跳过
                    }
                }
            }
        }

        ClassProfileStatsResponse.KnowledgeRadar radar = new ClassProfileStatsResponse.KnowledgeRadar();
        List<String> points = new ArrayList<>();
        List<BigDecimal> scores = new ArrayList<>();

        for (Map.Entry<String, KnowledgePointClassAgg> entry : aggMap.entrySet()) {
            points.add(entry.getKey());
            double masteryRate = 1.0 - (entry.getValue().getWrongStudentCount() / (double) students.size());
            scores.add(BigDecimal.valueOf(Math.max(0, masteryRate * 100)).setScale(2, RoundingMode.HALF_UP));
        }

        radar.setPoints(points);
        radar.setScores(scores);
        return radar;
    }

    /**
     * 构建成绩箱线图数据
     */
    private ClassProfileStatsResponse.ScoreBoxplot buildScoreBoxplot(Long classId) {
        List<Student> students = studentService.listByClass(classId);
        if (students.isEmpty()) {
            return new ClassProfileStatsResponse.ScoreBoxplot();
        }

        // 收集所有已批改的成绩
        List<BigDecimal> allScores = new ArrayList<>();
        for (Student student : students) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.edu.grading.entity.AnswerSheet> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(com.edu.grading.entity.AnswerSheet::getStudentId, student.getId())
                    .eq(com.edu.grading.entity.AnswerSheet::getStatus, 3) // 已批改
                    .isNotNull(com.edu.grading.entity.AnswerSheet::getTotalScore);

            List<com.edu.grading.entity.AnswerSheet> sheets = answerSheetMapper.selectList(wrapper);
            for (com.edu.grading.entity.AnswerSheet sheet : sheets) {
                if (sheet.getTotalScore() != null) {
                    allScores.add(sheet.getTotalScore());
                }
            }
        }

        if (allScores.isEmpty()) {
            return new ClassProfileStatsResponse.ScoreBoxplot();
        }

        // 排序
        allScores.sort(BigDecimal::compareTo);

        // 计算五数概括
        int size = allScores.size();
        BigDecimal min = allScores.get(0);
        BigDecimal max = allScores.get(size - 1);
        BigDecimal median = getPercentile(allScores, 50);
        BigDecimal q1 = getPercentile(allScores, 25);
        BigDecimal q3 = getPercentile(allScores, 75);

        // 检测异常值（简化：不检测）
        List<BigDecimal> outliers = new ArrayList<>();

        ClassProfileStatsResponse.ScoreBoxplot boxplot = new ClassProfileStatsResponse.ScoreBoxplot();
        boxplot.setMin(min);
        boxplot.setQ1(q1);
        boxplot.setMedian(median);
        boxplot.setQ3(q3);
        boxplot.setMax(max);
        boxplot.setOutliers(outliers);

        return boxplot;
    }

    /**
     * 计算百分位数
     */
    private BigDecimal getPercentile(List<BigDecimal> sortedList, double percentile) {
        int size = sortedList.size();
        double index = (percentile / 100.0) * (size - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return sortedList.get(lower);
        }

        // 线性插值
        BigDecimal lowerVal = sortedList.get(lower);
        BigDecimal upperVal = sortedList.get(upper);
        BigDecimal diff = upperVal.subtract(lowerVal);
        double fraction = index - lower;
        return lowerVal.add(diff.multiply(BigDecimal.valueOf(fraction)));
    }

    private static class KnowledgePointClassAgg {
        private Set<Long> studentIds = new HashSet<>();

        void addStudent(Long studentId) {
            studentIds.add(studentId);
        }

        int getWrongStudentCount() { return studentIds.size(); }
    }

    private static class KnowledgePointAgg {
        private int wrongCount = 0;
        private Set<Long> studentIds = new HashSet<>();

        void addStudent(Long studentId) {
            if (studentIds.add(studentId)) {
                wrongCount++;
            }
        }

        int getWrongCount() { return wrongCount; }
        int getWeakStudentCount() { return wrongCount; }
    }
}
