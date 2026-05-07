package com.edu.exam.controller;

import com.edu.common.entity.Result;
import com.edu.exam.dto.QuestionCreateRequest;
import com.edu.exam.dto.QuestionResponse;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 创建题目
     */
    @PostMapping
    public Result<QuestionResponse> createQuestion(@Valid @RequestBody QuestionCreateRequest request) {
        Question question = new Question();
        question.setBankId(request.getBankId());
        question.setSubject(request.getSubject());
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setContent(request.getContent());
        question.setOptions(request.getOptions());
        question.setAnswer(request.getAnswer());
        question.setAnswerAnalysis(request.getAnswerAnalysis());
        question.setKnowledgePoints(request.getKnowledgePoints());
        question.setCreatedBy(request.getCreatedBy());

        Question created = questionService.createQuestion(question);
        return Result.success(toResponse(created));
    }

    /**
     * 获取题库下的题目列表
     */
    @GetMapping("/bank/{bankId}")
    public Result<List<QuestionResponse>> listByBank(@PathVariable Long bankId) {
        List<Question> questions = questionService.listByBank(bankId);
        List<QuestionResponse> responses = questions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 查询题目（按条件筛选）
     */
    @GetMapping("/query")
    public Result<List<QuestionResponse>> queryQuestions(
            @RequestParam String subject,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String knowledgePoint) {
        List<Question> questions = questionService.queryQuestions(subject, type, difficulty, knowledgePoint);
        List<QuestionResponse> responses = questions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<QuestionResponse> getQuestion(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        return Result.success(toResponse(question));
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<Void> updateQuestion(@PathVariable Long id, @RequestBody QuestionCreateRequest request) {
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setContent(request.getContent());
        question.setOptions(request.getOptions());
        question.setAnswer(request.getAnswer());
        question.setAnswerAnalysis(request.getAnswerAnalysis());
        question.setKnowledgePoints(request.getKnowledgePoints());
        questionService.updateQuestion(question);
        return Result.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }

    private QuestionResponse toResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setBankId(question.getBankId());
        response.setSubject(question.getSubject());
        response.setQuestionType(question.getQuestionType());
        response.setDifficulty(question.getDifficulty());
        response.setContent(question.getContent());
        response.setOptions(question.getOptions());
        response.setAnswer(question.getAnswer());
        response.setAnswerAnalysis(question.getAnswerAnalysis());
        response.setKnowledgePoints(question.getKnowledgePoints());
        response.setSource(question.getSource());
        response.setCreatedBy(question.getCreatedBy());
        response.setCreatedAt(question.getCreatedAt());
        return response;
    }
}