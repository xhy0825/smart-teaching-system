package com.edu.grading.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.edu.common.entity.Result;
import com.edu.grading.dto.ScoreAnalysisResponse;
import com.edu.grading.dto.WrongQuestionResponse;
import com.edu.grading.entity.ScoreAnalysis;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.service.AnswerSheetService;
import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩分析控制器
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final ScoreAnalysisService scoreAnalysisService;
    private final StudentWrongQuestionService wrongQuestionService;
    private final AnswerSheetService answerSheetService;

    /**
     * 分析班级成绩
     */
    @PostMapping("/class")
    public Result<ScoreAnalysisResponse> analyzeClassScores(
            @RequestParam Long examPaperId,
            @RequestParam Long classId) {
        ScoreAnalysis analysis = scoreAnalysisService.analyzeClassScores(examPaperId, classId);
        if (analysis == null) {
            return Result.error("无答题数据");
        }
        return Result.success(toAnalysisResponse(analysis, examPaperId));
    }

    /**
     * 获取班级成绩分析
     */
    @GetMapping("/class")
    public Result<ScoreAnalysisResponse> getAnalysis(
            @RequestParam Long examPaperId,
            @RequestParam Long classId) {
        ScoreAnalysis analysis = scoreAnalysisService.getByExamAndClass(examPaperId, classId);
        if (analysis == null) {
            return Result.error("未找到分析数据");
        }
        return Result.success(toAnalysisResponse(analysis, examPaperId));
    }

    /**
     * 获取学生错题列表
     */
    @GetMapping("/wrong-questions/{studentId}")
    public Result<List<WrongQuestionResponse>> getWrongQuestions(@PathVariable Long studentId) {
        List<StudentWrongQuestion> wrongQuestions = wrongQuestionService.listByStudent(studentId);
        List<WrongQuestionResponse> responses = wrongQuestions.stream()
                .map(this::toWrongQuestionResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 标记错题已纠错
     */
    @PostMapping("/wrong-questions/corrected")
    public Result<Void> markCorrected(
            @RequestParam Long studentId,
            @RequestParam Long questionId) {
        wrongQuestionService.markCorrected(studentId, questionId);
        return Result.success();
    }

    /**
     * 获取高频错题
     */
    @GetMapping("/wrong-questions/frequent")
    public Result<List<WrongQuestionResponse>> getFrequentWrongQuestions(
            @RequestParam(defaultValue = "20") Long limit) {
        List<StudentWrongQuestion> wrongQuestions = wrongQuestionService.listFrequentWrong(limit);
        List<WrongQuestionResponse> responses = wrongQuestions.stream()
                .map(this::toWrongQuestionResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    private ScoreAnalysisResponse toAnalysisResponse(ScoreAnalysis analysis, Long examPaperId) {
        ScoreAnalysisResponse response = new ScoreAnalysisResponse();
        response.setId(analysis.getId());
        response.setExamPaperId(analysis.getExamPaperId());
        response.setClassId(analysis.getClassId());
        response.setAvgScore(analysis.getAvgScore());
        response.setMaxScore(analysis.getMaxScore());
        response.setMinScore(analysis.getMinScore());
        response.setPassRate(analysis.getPassRate());
        response.setExcellentRate(analysis.getExcellentRate());

        // 解析题目分析
        if (analysis.getQuestionAnalysis() != null) {
            JSONArray jsonArray = JSON.parseArray(analysis.getQuestionAnalysis());
            List<ScoreAnalysisResponse.QuestionAnalysisItem> items = jsonArray.stream()
                    .map(obj -> {
                        JSONObject json = (JSONObject) obj;
                        ScoreAnalysisResponse.QuestionAnalysisItem item = new ScoreAnalysisResponse.QuestionAnalysisItem();
                        item.setQuestionId(json.getLong("questionId"));
                        item.setSequence(json.getInteger("sequence"));
                        item.setMaxScore(json.getBigDecimal("maxScore"));
                        item.setAvgScore(json.getBigDecimal("avgScore"));
                        item.setCorrectRate(json.getBigDecimal("correctRate"));
                        return item;
                    })
                    .collect(Collectors.toList());
            response.setQuestionAnalysis(items);
        }

        // 统计人数
        response.setStudentCount(answerSheetService.countByExam(examPaperId));
        response.setGradedCount(answerSheetService.countGradedByExam(examPaperId));

        return response;
    }

    private WrongQuestionResponse toWrongQuestionResponse(StudentWrongQuestion wrongQuestion) {
        WrongQuestionResponse response = new WrongQuestionResponse();
        response.setId(wrongQuestion.getId());
        response.setStudentId(wrongQuestion.getStudentId());
        response.setQuestionId(wrongQuestion.getQuestionId());
        response.setExamPaperId(wrongQuestion.getExamPaperId());
        response.setWrongCount(wrongQuestion.getWrongCount());
        response.setLastWrongAt(wrongQuestion.getLastWrongAt());
        response.setCorrectedAt(wrongQuestion.getCorrectedAt());
        return response;
    }
}