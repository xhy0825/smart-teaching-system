package com.edu.user.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import com.edu.exam.mapper.ExamQuestionMapper;
import com.edu.exam.mapper.QuestionMapper;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.AnswerMapper;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
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
import java.util.stream.Collectors;

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
    private final ExamQuestionMapper examQuestionMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final StudentWrongQuestionMapper studentWrongQuestionMapper;

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
            ClassProfileStatsResponse.KnowledgeRadar radar = buildKnowledgeRadar(classId);
            response.setKnowledgeRadar(radar);
            log.debug("知识点雷达图数据: points={}, scores={}", radar != null ? radar.getPoints() : null, radar != null ? radar.getScores() : null);

            // 5. 成绩箱线图
            ClassProfileStatsResponse.ScoreBoxplot boxplot = buildScoreBoxplot(classId);
            response.setScoreBoxplot(boxplot);
            log.debug("成绩箱线图数据: min={}, q1={}, median={}, q3={}, max={}",
                    boxplot != null ? boxplot.getMin() : null,
                    boxplot != null ? boxplot.getQ1() : null,
                    boxplot != null ? boxplot.getMedian() : null,
                    boxplot != null ? boxplot.getQ3() : null,
                    boxplot != null ? boxplot.getMax() : null);

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

        List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());

        // 1. 批量查询班级所有学生的答题卡（已批改）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AnswerSheet> asWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        asWrapper.in(AnswerSheet::getStudentId, studentIds).eq(AnswerSheet::getStatus, 3);
        List<AnswerSheet> answerSheets = answerSheetMapper.selectList(asWrapper);

        // 2. 批量查询这些答题卡的所有答案
        if (answerSheets.isEmpty()) {
            return result;
        }
        List<Long> answerSheetIds = answerSheets.stream().map(AnswerSheet::getId).collect(Collectors.toList());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Answer> answerWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        answerWrapper.in(Answer::getAnswerSheetId, answerSheetIds);
        List<Answer> answers = answerMapper.selectList(answerWrapper);

        // 3. 获取所有 examQuestionId，批量查询 exam_question 获取 question_id
        Set<Long> examQuestionIds = answers.stream().map(Answer::getExamQuestionId).collect(Collectors.toSet());
        Map<Long, Long> eqToQuestionId = new HashMap<>();
        if (!examQuestionIds.isEmpty()) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamQuestion> eqWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            eqWrapper.in(ExamQuestion::getId, examQuestionIds);
            List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
            for (ExamQuestion eq : examQuestions) {
                eqToQuestionId.put(eq.getId(), eq.getQuestionId());
            }
        }

        // 4. 批量查询题目，解析知识点，统计每个知识点的总答题数
        Map<String, Integer> pointTotalCount = new HashMap<>();
        if (!eqToQuestionId.isEmpty()) {
            Set<Long> questionIds = new HashSet<>(eqToQuestionId.values());
            List<Question> questions = questionMapper.selectBatchIds(new ArrayList<>(questionIds));
            Map<Long, Question> questionMap = new HashMap<>();
            for (Question q : questions) {
                questionMap.put(q.getId(), q);
            }

            for (Answer answer : answers) {
                Long questionId = eqToQuestionId.get(answer.getExamQuestionId());
                if (questionId == null) continue;
                Question question = questionMap.get(questionId);
                if (question == null || question.getKnowledgePoints() == null) continue;
                try {
                    JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                    if (points != null) {
                        for (int i = 0; i < points.size(); i++) {
                            String point = points.getString(i);
                            pointTotalCount.merge(point, 1, Integer::sum);
                        }
                    }
                } catch (Exception e) {
                    // 解析失败跳过
                }
            }
        }

        // 5. 查询班级所有学生的错题，统计每个知识点的错题数
        Map<String, Integer> pointWrongCount = new HashMap<>();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentWrongQuestion> wqWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wqWrapper.in(StudentWrongQuestion::getStudentId, studentIds);
        List<StudentWrongQuestion> wrongQuestions = studentWrongQuestionMapper.selectList(wqWrapper);

        for (StudentWrongQuestion wq : wrongQuestions) {
            Question question = questionMapper.selectById(wq.getQuestionId());
            if (question == null || question.getKnowledgePoints() == null) continue;
            try {
                JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                if (points != null) {
                    int wrongCount = wq.getWrongCount() != null ? wq.getWrongCount() : 1;
                    for (int i = 0; i < points.size(); i++) {
                        String point = points.getString(i);
                        pointWrongCount.merge(point, wrongCount, Integer::sum);
                    }
                }
            } catch (Exception e) {
                // 解析失败跳过
            }
        }

        // 6. 计算掌握率
        for (Map.Entry<String, Integer> entry : pointTotalCount.entrySet()) {
            String point = entry.getKey();
            int totalCount = entry.getValue();
            int wrongCount = pointWrongCount.getOrDefault(point, 0);
            int correctCount = Math.max(0, totalCount - wrongCount);
            double masteryRate = totalCount > 0 ? (correctCount * 100.0) / totalCount : 0.0;

            ClassProfileStatsResponse.KnowledgeMastery km = new ClassProfileStatsResponse.KnowledgeMastery();
            km.setKnowledgePoint(point);
            km.setAvgMasteryRate(BigDecimal.valueOf(masteryRate).setScale(2, RoundingMode.HALF_UP));
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
     * 构建班级知识点雷达图数据，复用 buildKnowledgeMastery() 的结果
     */
    private ClassProfileStatsResponse.KnowledgeRadar buildKnowledgeRadar(Long classId) {
        List<ClassProfileStatsResponse.KnowledgeMastery> masteryList = buildKnowledgeMastery(classId);
        ClassProfileStatsResponse.KnowledgeRadar radar = new ClassProfileStatsResponse.KnowledgeRadar();
        List<String> points = new ArrayList<>();
        List<BigDecimal> scores = new ArrayList<>();

        for (ClassProfileStatsResponse.KnowledgeMastery km : masteryList) {
            points.add(km.getKnowledgePoint());
            scores.add(km.getAvgMasteryRate());
        }

        radar.setPoints(points);
        radar.setScores(scores);
        return radar;
    }

    /**
     * 构建成绩箱线图数据（优化：批量查询）
     */
    private ClassProfileStatsResponse.ScoreBoxplot buildScoreBoxplot(Long classId) {
        List<Student> students = studentService.listByClass(classId);
        if (students.isEmpty()) {
            return new ClassProfileStatsResponse.ScoreBoxplot();
        }

        // 批量查询所有学生的答题卡（已批改，有成绩）
        List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.edu.grading.entity.AnswerSheet> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.in(com.edu.grading.entity.AnswerSheet::getStudentId, studentIds)
                .eq(com.edu.grading.entity.AnswerSheet::getStatus, 3)
                .isNotNull(com.edu.grading.entity.AnswerSheet::getTotalScore);
        List<com.edu.grading.entity.AnswerSheet> allSheets = answerSheetMapper.selectList(wrapper);

        // 收集所有已批改的成绩
        List<BigDecimal> allScores = new ArrayList<>();
        for (com.edu.grading.entity.AnswerSheet sheet : allSheets) {
            if (sheet.getTotalScore() != null) {
                allScores.add(sheet.getTotalScore());
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
}