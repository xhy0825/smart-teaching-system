package com.edu.grading.controller;

import com.edu.common.entity.Result;
import com.edu.grading.dto.AnswerSheetResponse;
import com.edu.grading.dto.AnswerSubmitRequest;
import com.edu.grading.dto.AnswerResponse;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.service.AnswerService;
import com.edu.grading.service.AnswerSheetService;
import com.edu.grading.service.GradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 答题控制器
 */
@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerSheetService answerSheetService;
    private final AnswerService answerService;
    private final GradingService gradingService;

    /**
     * 创建答题卡
     */
    @PostMapping("/sheet")
    public Result<AnswerSheetResponse> createAnswerSheet(
            @RequestParam Long examPaperId,
            @RequestParam Long studentId) {
        AnswerSheet sheet = answerSheetService.createAnswerSheet(examPaperId, studentId);
        return Result.success(toSheetResponse(sheet));
    }

    /**
     * 提交答案
     */
    @PostMapping
    public Result<AnswerResponse> submitAnswer(@Valid @RequestBody AnswerSubmitRequest request) {
        Answer answer = answerService.saveAnswer(
                request.getAnswerSheetId(),
                request.getExamQuestionId(),
                request.getStudentAnswer()
        );
        return Result.success(toAnswerResponse(answer));
    }

    /**
     * 提交答题卡
     */
    @PostMapping("/sheet/{id}/submit")
    public Result<Void> submitAnswerSheet(@PathVariable Long id) {
        answerSheetService.submitAnswerSheet(id);
        return Result.success();
    }

    /**
     * 批改答题卡
     */
    @PostMapping("/sheet/{id}/grade")
    public Result<Void> gradeAnswerSheet(
            @PathVariable Long id,
            @RequestParam Long gradedBy) {
        gradingService.gradeAnswerSheet(id, gradedBy);
        return Result.success();
    }

    /**
     * 获取答题卡详情
     */
    @GetMapping("/sheet/{id}")
    public Result<AnswerSheetResponse> getAnswerSheet(@PathVariable Long id) {
        AnswerSheet sheet = answerSheetService.getById(id);
        if (sheet == null) {
            return Result.error("答题卡不存在");
        }
        return Result.success(toSheetResponse(sheet));
    }

    /**
     * 获取试卷的所有答题卡
     */
    @GetMapping("/sheet/list/{examPaperId}")
    public Result<List<AnswerSheetResponse>> listByExam(@PathVariable Long examPaperId) {
        List<AnswerSheet> sheets = answerSheetService.listByExam(examPaperId);
        List<AnswerSheetResponse> responses = sheets.stream()
                .map(this::toSheetResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取答题卡的答案列表
     */
    @GetMapping("/list/{answerSheetId}")
    public Result<List<AnswerResponse>> listAnswers(@PathVariable Long answerSheetId) {
        List<Answer> answers = answerService.listByAnswerSheet(answerSheetId);
        List<AnswerResponse> responses = answers.stream()
                .map(this::toAnswerResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    private AnswerSheetResponse toSheetResponse(AnswerSheet sheet) {
        AnswerSheetResponse response = new AnswerSheetResponse();
        response.setId(sheet.getId());
        response.setTenantId(sheet.getTenantId());
        response.setExamPaperId(sheet.getExamPaperId());
        response.setStudentId(sheet.getStudentId());
        response.setStatus(sheet.getStatus());
        response.setTotalScore(sheet.getTotalScore());
        response.setSubmitTime(sheet.getSubmitTime());
        response.setGradingTime(sheet.getGradingTime());
        response.setGradedBy(sheet.getGradedBy());
        response.setCreatedAt(sheet.getCreatedAt());
        return response;
    }

    private AnswerResponse toAnswerResponse(Answer answer) {
        AnswerResponse response = new AnswerResponse();
        response.setId(answer.getId());
        response.setAnswerSheetId(answer.getAnswerSheetId());
        response.setExamQuestionId(answer.getExamQuestionId());
        response.setStudentAnswer(answer.getStudentAnswer());
        response.setIsCorrect(answer.getIsCorrect());
        response.setScore(answer.getScore());
        response.setAiScore(answer.getAiScore());
        response.setManualScore(answer.getManualScore());
        response.setAiAnalysis(answer.getAiAnalysis());
        response.setGradedAt(answer.getGradedAt());
        return response;
    }
}