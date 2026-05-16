package com.edu.user.service;

import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassProfileService {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService studentWrongQuestionService;
    private final StudentProfileService studentProfileService;

    public ClassProfileStatsResponse getClassStats(Long classId) {
        ClassProfileStatsResponse response = new ClassProfileStatsResponse();

        // TODO: 调用各服务填充数据
        // 1. baseStats + distribution 通过 scoreAnalysisService 获取
        // 2. knowledgeMastery 通过 studentWrongQuestionService 聚合

        return response;
    }
}
