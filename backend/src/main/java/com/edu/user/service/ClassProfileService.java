package com.edu.user.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import com.edu.user.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClassProfileService {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService studentWrongQuestionService;
    private final com.edu.user.service.StudentProfileService studentProfileService;
    private final com.edu.user.service.StudentService studentService;
    private final QuestionService questionService;

    public ClassProfileStatsResponse getClassStats(Long classId) {
        ClassProfileStatsResponse response = new ClassProfileStatsResponse();

        // 1. 获取成绩分析数据（从 score_analysis 表）
        ScoreAnalysis latestAnalysis = scoreAnalysisService.getLatestByClassId(classId);

        ClassProfileStatsResponse.BaseStats baseStats = new ClassProfileStatsResponse.BaseStats();
        if (latestAnalysis != null) {
            baseStats.setAvgScore(latestAnalysis.getAvgScore());
            baseStats.setMaxScore(latestAnalysis.getMaxScore());
            baseStats.setMinScore(latestAnalysis.getMinScore());
            baseStats.setPassRate(latestAnalysis.getPassRate());
            baseStats.setExcellentRate(latestAnalysis.getExcellentRate());
            baseStats.setStudentCount(0);
            baseStats.setGradedCount(0);
        } else {
            baseStats.setAvgScore(BigDecimal.ZERO);
            baseStats.setMaxScore(BigDecimal.ZERO);
            baseStats.setMinScore(BigDecimal.ZERO);
            baseStats.setPassRate(BigDecimal.ZERO);
            baseStats.setExcellentRate(BigDecimal.ZERO);
            baseStats.setStudentCount(0);
            baseStats.setGradedCount(0);
        }
        response.setBaseStats(baseStats);

        // 2. 分数段分布（暂时返回空列表，后续扩展）
        response.setDistribution(new ArrayList<>());

        // 3. 知识点掌握分布
        response.setKnowledgeMastery(buildKnowledgeMastery(classId));

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
