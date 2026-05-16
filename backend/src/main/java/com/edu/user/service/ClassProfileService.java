package com.edu.user.service;

import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassProfileService {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService studentWrongQuestionService;
    private final StudentProfileService studentProfileService;

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
            // studentCount 和 gradedCount 暂时设为0，后续从其他表获取
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

        // 3. 知识点掌握分布（Task 4 实现）
        response.setKnowledgeMastery(new ArrayList<>());

        return response;
    }
}
