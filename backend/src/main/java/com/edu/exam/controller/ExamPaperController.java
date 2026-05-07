package com.edu.exam.controller;

import com.edu.common.entity.Result;
import com.edu.exam.dto.ExamGenerateRequest;
import com.edu.exam.dto.ExamPaperCreateRequest;
import com.edu.exam.dto.ExamPaperResponse;
import com.edu.exam.dto.ExamQuestionResponse;
import com.edu.exam.dto.QuestionResponse;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import com.edu.exam.service.ExamGenerateService;
import com.edu.exam.service.ExamPaperService;
import com.edu.exam.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 试卷控制器
 */
@RestController
@RequestMapping("/api/exam-paper")
@RequiredArgsConstructor
public class ExamPaperController {

    private final ExamPaperService examPaperService;
    private final ExamGenerateService examGenerateService;
    private final QuestionService questionService;

    /**
     * 创建试卷（手动）
     */
    @PostMapping
    public Result<ExamPaperResponse> createPaper(@Valid @RequestBody ExamPaperCreateRequest request) {
        ExamPaper paper = new ExamPaper();
        paper.setTitle(request.getTitle());
        paper.setSubject(request.getSubject());
        paper.setTemplateId(request.getTemplateId());
        paper.setGradeId(request.getGradeId());
        paper.setClassId(request.getClassId());
        paper.setTotalScore(request.getTotalScore());
        paper.setTimeLimit(request.getTimeLimit());
        paper.setCreatedBy(request.getCreatedBy());

        ExamPaper created = examPaperService.createPaper(paper);
        return Result.success(toResponse(created));
    }

    /**
     * AI生成试卷
     */
    @PostMapping("/generate")
    public Result<ExamPaperResponse> generatePaper(@Valid @RequestBody ExamGenerateRequest request) {
        ExamPaper paper = examGenerateService.generateExam(request);
        return Result.success(toResponseWithQuestions(paper));
    }

    /**
     * 获取试卷列表
     */
    @GetMapping
    public Result<List<ExamPaperResponse>> listPapers() {
        List<ExamPaper> papers = examPaperService.listByTenant();
        List<ExamPaperResponse> responses = papers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取班级试卷列表
     */
    @GetMapping("/class/{classId}")
    public Result<List<ExamPaperResponse>> listByClass(@PathVariable Long classId) {
        List<ExamPaper> papers = examPaperService.listByClass(classId);
        List<ExamPaperResponse> responses = papers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取试卷详情（含题目）
     */
    @GetMapping("/{id}")
    public Result<ExamPaperResponse> getPaper(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getPaperById(id);
        if (paper == null) {
            return Result.error("试卷不存在");
        }
        return Result.success(toResponseWithQuestions(paper));
    }

    /**
     * 更新试卷
     */
    @PutMapping("/{id}")
    public Result<Void> updatePaper(@PathVariable Long id, @RequestBody ExamPaperCreateRequest request) {
        ExamPaper paper = examPaperService.getPaperById(id);
        if (paper == null) {
            return Result.error("试卷不存在");
        }
        paper.setTitle(request.getTitle());
        paper.setSubject(request.getSubject());
        paper.setGradeId(request.getGradeId());
        paper.setClassId(request.getClassId());
        paper.setTotalScore(request.getTotalScore());
        paper.setTimeLimit(request.getTimeLimit());
        examPaperService.updatePaper(paper);
        return Result.success();
    }

    /**
     * 发布试卷
     */
    @PutMapping("/{id}/publish")
    public Result<Void> publishPaper(@PathVariable Long id) {
        examPaperService.publishPaper(id);
        return Result.success();
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePaper(@PathVariable Long id) {
        examPaperService.deletePaper(id);
        return Result.success();
    }

    /**
     * 添加题目到试卷
     */
    @PostMapping("/{paperId}/question")
    public Result<Void> addQuestion(
            @PathVariable Long paperId,
            @RequestParam Long questionId,
            @RequestParam Integer sequence,
            @RequestParam BigDecimal score) {
        examPaperService.addQuestionToPaper(paperId, questionId, sequence, score);
        return Result.success();
    }

    private ExamPaperResponse toResponse(ExamPaper paper) {
        ExamPaperResponse response = new ExamPaperResponse();
        response.setId(paper.getId());
        response.setTenantId(paper.getTenantId());
        response.setTemplateId(paper.getTemplateId());
        response.setTitle(paper.getTitle());
        response.setSubject(paper.getSubject());
        response.setGradeId(paper.getGradeId());
        response.setClassId(paper.getClassId());
        response.setTotalScore(paper.getTotalScore());
        response.setTimeLimit(paper.getTimeLimit());
        response.setStatus(paper.getStatus());
        response.setCreatedBy(paper.getCreatedBy());
        response.setCreatedAt(paper.getCreatedAt());
        response.setPublishedAt(paper.getPublishedAt());
        return response;
    }

    private ExamPaperResponse toResponseWithQuestions(ExamPaper paper) {
        ExamPaperResponse response = toResponse(paper);
        List<ExamQuestion> examQuestions = examPaperService.getPaperQuestions(paper.getId());
        List<ExamQuestionResponse> questionResponses = examQuestions.stream()
                .map(this::toExamQuestionResponse)
                .collect(Collectors.toList());
        response.setQuestions(questionResponses);
        return response;
    }

    private ExamQuestionResponse toExamQuestionResponse(ExamQuestion eq) {
        ExamQuestionResponse response = new ExamQuestionResponse();
        response.setId(eq.getId());
        response.setExamPaperId(eq.getExamPaperId());
        response.setQuestionId(eq.getQuestionId());
        response.setSequence(eq.getSequence());
        response.setScore(eq.getScore());

        Question question = questionService.getQuestionById(eq.getQuestionId());
        if (question != null) {
            response.setQuestion(toQuestionResponse(question));
        }
        return response;
    }

    private QuestionResponse toQuestionResponse(Question q) {
        QuestionResponse response = new QuestionResponse();
        response.setId(q.getId());
        response.setBankId(q.getBankId());
        response.setSubject(q.getSubject());
        response.setQuestionType(q.getQuestionType());
        response.setDifficulty(q.getDifficulty());
        response.setContent(q.getContent());
        response.setOptions(q.getOptions());
        response.setAnswer(q.getAnswer());
        response.setAnswerAnalysis(q.getAnswerAnalysis());
        response.setKnowledgePoints(q.getKnowledgePoints());
        response.setSource(q.getSource());
        response.setCreatedBy(q.getCreatedBy());
        response.setCreatedAt(q.getCreatedAt());
        return response;
    }
}