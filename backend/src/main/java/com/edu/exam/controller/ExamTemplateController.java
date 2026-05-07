package com.edu.exam.controller;

import com.edu.common.entity.Result;
import com.edu.exam.dto.ExamTemplateCreateRequest;
import com.edu.exam.dto.ExamTemplateResponse;
import com.edu.exam.entity.ExamTemplate;
import com.edu.exam.service.ExamTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 试卷模板控制器
 */
@RestController
@RequestMapping("/api/exam-template")
@RequiredArgsConstructor
public class ExamTemplateController {

    private final ExamTemplateService examTemplateService;

    /**
     * 创建模板
     */
    @PostMapping
    public Result<ExamTemplateResponse> createTemplate(@Valid @RequestBody ExamTemplateCreateRequest request) {
        ExamTemplate template = new ExamTemplate();
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setTotalScore(request.getTotalScore());
        template.setTimeLimit(request.getTimeLimit());
        template.setStructure(request.getStructure());
        template.setCreatedBy(request.getCreatedBy());

        ExamTemplate created = examTemplateService.createTemplate(template);
        return Result.success(toResponse(created));
    }

    /**
     * 获取模板列表
     */
    @GetMapping
    public Result<List<ExamTemplateResponse>> listTemplates() {
        List<ExamTemplate> templates = examTemplateService.listByTenant();
        List<ExamTemplateResponse> responses = templates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 按学科获取模板列表
     */
    @GetMapping("/subject/{subject}")
    public Result<List<ExamTemplateResponse>> listBySubject(@PathVariable String subject) {
        List<ExamTemplate> templates = examTemplateService.listBySubject(subject);
        List<ExamTemplateResponse> responses = templates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<ExamTemplateResponse> getTemplate(@PathVariable Long id) {
        ExamTemplate template = examTemplateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(toResponse(template));
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody ExamTemplateCreateRequest request) {
        ExamTemplate template = examTemplateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setTotalScore(request.getTotalScore());
        template.setTimeLimit(request.getTimeLimit());
        template.setStructure(request.getStructure());
        examTemplateService.updateTemplate(template);
        return Result.success();
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        examTemplateService.deleteTemplate(id);
        return Result.success();
    }

    private ExamTemplateResponse toResponse(ExamTemplate template) {
        ExamTemplateResponse response = new ExamTemplateResponse();
        response.setId(template.getId());
        response.setTenantId(template.getTenantId());
        response.setName(template.getName());
        response.setSubject(template.getSubject());
        response.setTotalScore(template.getTotalScore());
        response.setTimeLimit(template.getTimeLimit());
        response.setStructure(template.getStructure());
        response.setCreatedBy(template.getCreatedBy());
        response.setCreatedAt(template.getCreatedAt());
        return response;
    }
}