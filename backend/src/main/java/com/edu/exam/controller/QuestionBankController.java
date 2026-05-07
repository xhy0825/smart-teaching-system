package com.edu.exam.controller;

import com.edu.common.entity.Result;
import com.edu.exam.dto.QuestionBankCreateRequest;
import com.edu.exam.dto.QuestionBankResponse;
import com.edu.exam.entity.QuestionBank;
import com.edu.exam.service.QuestionBankService;
import com.edu.exam.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库控制器
 */
@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final QuestionService questionService;

    /**
     * 创建题库
     */
    @PostMapping
    public Result<QuestionBankResponse> createBank(@Valid @RequestBody QuestionBankCreateRequest request) {
        QuestionBank bank = new QuestionBank();
        bank.setName(request.getName());
        bank.setSubject(request.getSubject());
        bank.setGradeLevel(request.getGradeLevel());
        bank.setDescription(request.getDescription());
        bank.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : 0);
        bank.setCreatedBy(request.getCreatedBy());

        QuestionBank created = questionBankService.createBank(bank);
        return Result.success(toResponse(created));
    }

    /**
     * 获取题库列表
     */
    @GetMapping
    public Result<List<QuestionBankResponse>> listBanks() {
        List<QuestionBank> banks = questionBankService.listByTenant();
        List<QuestionBankResponse> responses = banks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 按学科获取题库列表
     */
    @GetMapping("/subject/{subject}")
    public Result<List<QuestionBankResponse>> listBySubject(@PathVariable String subject) {
        List<QuestionBank> banks = questionBankService.listBySubject(subject);
        List<QuestionBankResponse> responses = banks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取题库详情
     */
    @GetMapping("/{id}")
    public Result<QuestionBankResponse> getBank(@PathVariable Long id) {
        QuestionBank bank = questionBankService.getBankById(id);
        if (bank == null) {
            return Result.error("题库不存在");
        }
        return Result.success(toResponse(bank));
    }

    /**
     * 更新题库
     */
    @PutMapping("/{id}")
    public Result<Void> updateBank(@PathVariable Long id, @RequestBody QuestionBankCreateRequest request) {
        QuestionBank bank = questionBankService.getBankById(id);
        if (bank == null) {
            return Result.error("题库不存在");
        }
        bank.setName(request.getName());
        bank.setSubject(request.getSubject());
        bank.setGradeLevel(request.getGradeLevel());
        bank.setDescription(request.getDescription());
        bank.setIsPublic(request.getIsPublic());
        questionBankService.updateBank(bank);
        return Result.success();
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBank(@PathVariable Long id) {
        questionBankService.deleteBank(id);
        return Result.success();
    }

    private QuestionBankResponse toResponse(QuestionBank bank) {
        QuestionBankResponse response = new QuestionBankResponse();
        response.setId(bank.getId());
        response.setTenantId(bank.getTenantId());
        response.setName(bank.getName());
        response.setSubject(bank.getSubject());
        response.setGradeLevel(bank.getGradeLevel());
        response.setDescription(bank.getDescription());
        response.setIsPublic(bank.getIsPublic());
        response.setCreatedBy(bank.getCreatedBy());
        response.setCreatedAt(bank.getCreatedAt());
        response.setQuestionCount(questionService.countByBank(bank.getId()));
        return response;
    }
}