package com.edu.grading.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.service.ExamPaperService;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.mapper.ScoreAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreAnalysisService extends ServiceImpl<ScoreAnalysisMapper, ScoreAnalysis> {

    private final AnswerSheetService answerSheetService;
    private final AnswerService answerService;
    private final ExamPaperService examPaperService;

    /**
     * 分析班级成绩
     */
    @Transactional
    public ScoreAnalysis analyzeClassScores(Long examPaperId, Long classId) {
        // 获取班级答题卡
        List<AnswerSheet> answerSheets = answerSheetService.listByExam(examPaperId);
        // 过滤本班级的答题卡（需要关联学生信息，这里简化处理）
        List<AnswerSheet> classAnswerSheets = answerSheets;

        if (classAnswerSheets.isEmpty()) {
            log.warn("班级无答题记录: examPaperId={}, classId={}", examPaperId, classId);
            return null;
        }

        // 计算统计数据
        List<BigDecimal> scores = classAnswerSheets.stream()
                .filter(as -> as.getTotalScore() != null)
                .map(AnswerSheet::getTotalScore)
                .collect(Collectors.toList());

        BigDecimal avgScore = calculateAvg(scores);
        BigDecimal maxScore = calculateMax(scores);
        BigDecimal minScore = calculateMin(scores);
        BigDecimal passRate = calculatePassRate(scores, avgScore);
        BigDecimal excellentRate = calculateExcellentRate(scores);

        // 分析题目得分率
        JSONArray questionAnalysis = analyzeQuestions(examPaperId, classAnswerSheets);

        // 保存分析结果
        LambdaQueryWrapper<ScoreAnalysis> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ScoreAnalysis::getExamPaperId, examPaperId)
                .eq(ScoreAnalysis::getClassId, classId);
        ScoreAnalysis existing = baseMapper.selectOne(checkWrapper);

        ScoreAnalysis analysis;
        if (existing != null) {
            analysis = existing;
        } else {
            analysis = new ScoreAnalysis();
            analysis.setExamPaperId(examPaperId);
            analysis.setClassId(classId);
        }

        analysis.setAvgScore(avgScore);
        analysis.setMaxScore(maxScore);
        analysis.setMinScore(minScore);
        analysis.setPassRate(passRate);
        analysis.setExcellentRate(excellentRate);
        analysis.setQuestionAnalysis(questionAnalysis.toJSONString());

        if (existing != null) {
            baseMapper.updateById(analysis);
        } else {
            baseMapper.insert(analysis);
        }

        log.info("成绩分析完成: examPaperId={}, avgScore={}", examPaperId, avgScore);
        return analysis;
    }

    public ScoreAnalysis getByExamAndClass(Long examPaperId, Long classId) {
        LambdaQueryWrapper<ScoreAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreAnalysis::getExamPaperId, examPaperId)
                .eq(ScoreAnalysis::getClassId, classId);
        return baseMapper.selectOne(wrapper);
    }

    public ScoreAnalysis getLatestByClassId(Long classId) {
        return baseMapper.selectLatestByClassId(classId);
    }

    private BigDecimal calculateAvg(List<BigDecimal> scores) {
        if (scores.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMax(List<BigDecimal> scores) {
        return scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateMin(List<BigDecimal> scores) {
        return scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculatePassRate(List<BigDecimal> scores, BigDecimal avgScore) {
        if (scores.isEmpty()) return BigDecimal.ZERO;
        // 及格线为平均分的60%
        BigDecimal passLine = avgScore.multiply(new BigDecimal("0.6"));
        long passCount = scores.stream()
                .filter(s -> s.compareTo(passLine) >= 0)
                .count();
        return BigDecimal.valueOf(passCount)
                .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateExcellentRate(List<BigDecimal> scores) {
        if (scores.isEmpty()) return BigDecimal.ZERO;
        // 优秀线为平均分的90%
        BigDecimal avgScore = calculateAvg(scores);
        BigDecimal excellentLine = avgScore.multiply(new BigDecimal("0.9"));
        long excellentCount = scores.stream()
                .filter(s -> s.compareTo(excellentLine) >= 0)
                .count();
        return BigDecimal.valueOf(excellentCount)
                .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);
    }

    private JSONArray analyzeQuestions(Long examPaperId, List<AnswerSheet> answerSheets) {
        JSONArray analysis = new JSONArray();
        List<ExamQuestion> examQuestions = examPaperService.getPaperQuestions(examPaperId);

        for (ExamQuestion eq : examQuestions) {
            JSONObject questionStat = new JSONObject();
            questionStat.put("questionId", eq.getQuestionId());
            questionStat.put("sequence", eq.getSequence());
            questionStat.put("maxScore", eq.getScore());

            // 统计该题得分
            List<BigDecimal> questionScores = answerSheets.stream()
                    .map(as -> answerService.getBySheetAndQuestion(as.getId(), eq.getId()))
                    .filter(a -> a != null && a.getScore() != null)
                    .map(Answer::getScore)
                    .collect(Collectors.toList());

            if (!questionScores.isEmpty()) {
                BigDecimal avg = calculateAvg(questionScores);
                questionStat.put("avgScore", avg);
                questionStat.put("correctRate", avg.divide(eq.getScore(), 4, RoundingMode.HALF_UP));
            }

            analysis.add(questionStat);
        }

        return analysis;
    }
}