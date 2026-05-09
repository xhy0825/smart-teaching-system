package com.edu.ppt.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.ppt.dto.PptGenerateRequest;
import com.edu.ppt.dto.PptResponse;
import com.edu.ppt.dto.PptSlideResponse;
import com.edu.ppt.entity.PptDocument;
import com.edu.ppt.mapper.PptDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PptService {

    private final PptDocumentMapper pptDocumentMapper;
    private final QuestionService questionService;

    @Transactional
    public PptResponse generatePpt(PptGenerateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }

        // 创建PPT文档
        PptDocument document = new PptDocument();
        document.setTenantId(tenantId);
        document.setTitle(request.getTitle());
        document.setSubject(request.getSubject());
        document.setTemplateType(request.getTemplateType());
        document.setCreatedBy(request.getCreatedBy());
        document.setCreatedAt(LocalDateTime.now());
        document.setDeleted(0);

        // 生成幻灯片内容
        List<PptSlideResponse> slides = generateSlides(request);
        document.setPageCount(slides.size());

        // 存储内容JSON
        JSONArray slidesJson = new JSONArray();
        for (PptSlideResponse slide : slides) {
            JSONObject slideJson = new JSONObject();
            slideJson.put("pageIndex", slide.getPageIndex());
            slideJson.put("title", slide.getTitle());
            slideJson.put("content", slide.getContent());
            if (slide.getQuestionIds() != null) {
                slideJson.put("questionIds", slide.getQuestionIds());
            }
            slidesJson.add(slideJson);
        }
        document.setContentJson(slidesJson.toJSONString());

        // 模拟生成文件路径（实际可接入PPT生成库）
        document.setFilePath("/ppt/" + document.getId() + ".pptx");

        pptDocumentMapper.insert(document);
        log.info("创建PPT文档: id={}, title={}, pages={}", document.getId(), document.getTitle(), slides.size());

        return toResponse(document, slides);
    }

    private List<PptSlideResponse> generateSlides(PptGenerateRequest request) {
        List<PptSlideResponse> slides = new ArrayList<>();

        // 标题页
        PptSlideResponse titleSlide = new PptSlideResponse();
        titleSlide.setPageIndex(1);
        titleSlide.setTitle(request.getTitle());
        titleSlide.setContent("学科: " + getSubjectName(request.getSubject()) + "\n年级: " + request.getGradeId());
        slides.add(titleSlide);

        // 根据模板类型生成不同内容
        switch (request.getTemplateType()) {
            case "LESSON":
                slides.addAll(generateLessonSlides(request));
                break;
            case "EXAM":
                slides.addAll(generateExamSlides(request));
                break;
            case "SUMMARY":
                slides.addAll(generateSummarySlides(request));
                break;
            default:
                slides.addAll(generateLessonSlides(request));
        }

        // 更新页码
        for (int i = 0; i < slides.size(); i++) {
            slides.get(i).setPageIndex(i + 1);
        }

        return slides;
    }

    private List<PptSlideResponse> generateLessonSlides(PptGenerateRequest request) {
        List<PptSlideResponse> slides = new ArrayList<>();

        // 知识点介绍页
        if (request.getKnowledgePoints() != null && !request.getKnowledgePoints().isEmpty()) {
            PptSlideResponse kpSlide = new PptSlideResponse();
            kpSlide.setTitle("知识点概览");
            StringBuilder content = new StringBuilder("本节课涉及以下知识点:\n\n");
            for (String kp : request.getKnowledgePoints()) {
                content.append("- ").append(kp).append("\n");
            }
            kpSlide.setContent(content.toString());
            slides.add(kpSlide);
        }

        // 题目页面
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            for (Long questionId : request.getQuestionIds()) {
                Question question = questionService.getQuestionById(questionId);
                if (question != null) {
                    PptSlideResponse questionSlide = new PptSlideResponse();
                    questionSlide.setTitle("例题讲解");
                    questionSlide.setContent(formatQuestionContent(question));
                    questionSlide.setQuestionIds(List.of(questionId));
                    slides.add(questionSlide);

                    // 答案解析页
                    PptSlideResponse analysisSlide = new PptSlideResponse();
                    analysisSlide.setTitle("答案与解析");
                    analysisSlide.setContent("答案: " + question.getAnswer() + "\n\n解析: " + question.getAnswerAnalysis());
                    analysisSlide.setQuestionIds(List.of(questionId));
                    slides.add(analysisSlide);
                }
            }
        }

        // 自定义内容页
        if (request.getCustomContent() != null && !request.getCustomContent().isEmpty()) {
            PptSlideResponse customSlide = new PptSlideResponse();
            customSlide.setTitle("补充内容");
            customSlide.setContent(request.getCustomContent());
            slides.add(customSlide);
        }

        return slides;
    }

    private List<PptSlideResponse> generateExamSlides(PptGenerateRequest request) {
        List<PptSlideResponse> slides = new ArrayList<>();

        // 考试说明页
        PptSlideResponse introSlide = new PptSlideResponse();
        introSlide.setTitle("考试说明");
        introSlide.setContent("请认真阅读每道题目，按要求作答。\n注意答题时间分配。");
        slides.add(introSlide);

        // 题目页面（不含答案）
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            int index = 1;
            for (Long questionId : request.getQuestionIds()) {
                Question question = questionService.getQuestionById(questionId);
                if (question != null) {
                    PptSlideResponse questionSlide = new PptSlideResponse();
                    questionSlide.setTitle("第" + index + "题");
                    questionSlide.setContent(formatQuestionContentNoAnswer(question));
                    questionSlide.setQuestionIds(List.of(questionId));
                    slides.add(questionSlide);
                    index++;
                }
            }
        }

        return slides;
    }

    private List<PptSlideResponse> generateSummarySlides(PptGenerateRequest request) {
        List<PptSlideResponse> slides = new ArrayList<>();

        // 知识点总结
        if (request.getKnowledgePoints() != null && !request.getKnowledgePoints().isEmpty()) {
            for (String kp : request.getKnowledgePoints()) {
                PptSlideResponse kpSlide = new PptSlideResponse();
                kpSlide.setTitle("知识点: " + kp);
                kpSlide.setContent("重点内容总结:\n\n请教师补充具体知识点总结内容。");
                slides.add(kpSlide);
            }
        }

        // 易错题分析
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            PptSlideResponse errorSlide = new PptSlideResponse();
            errorSlide.setTitle("易错题分析");
            StringBuilder content = new StringBuilder();
            for (Long questionId : request.getQuestionIds()) {
                Question question = questionService.getQuestionById(questionId);
                if (question != null) {
                    content.append("题目: ").append(question.getContent()).append("\n");
                    content.append("常见错误: 请补充\n\n");
                }
            }
            errorSlide.setContent(content.toString());
            slides.add(errorSlide);
        }

        return slides;
    }

    private String formatQuestionContent(Question question) {
        StringBuilder sb = new StringBuilder();
        sb.append("题型: ").append(getTypeName(question.getQuestionType())).append("\n\n");
        sb.append("题目: ").append(question.getContent()).append("\n\n");

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            sb.append("选项:\n").append(question.getOptions()).append("\n\n");
        }

        sb.append("答案: ").append(question.getAnswer()).append("\n\n");
        sb.append("解析: ").append(question.getAnswerAnalysis());

        return sb.toString();
    }

    private String formatQuestionContentNoAnswer(Question question) {
        StringBuilder sb = new StringBuilder();
        sb.append("题型: ").append(getTypeName(question.getQuestionType())).append("\n\n");
        sb.append("题目: ").append(question.getContent()).append("\n\n");

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            sb.append("选项:\n").append(question.getOptions());
        }

        return sb.toString();
    }

    private String getTypeName(String type) {
        switch (type) {
            case "CHOICE": return "选择题";
            case "FILL": return "填空题";
            case "JUDGE": return "判断题";
            case "CALCULATION": return "计算题";
            default: return "未知题型";
        }
    }

    private String getSubjectName(String subject) {
        switch (subject) {
            case "MATH": return "数学";
            case "PHYSICS": return "物理";
            case "CHEMISTRY": return "化学";
            case "ENGLISH": return "英语";
            default: return subject;
        }
    }

    public List<PptResponse> listByTenant() {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<PptDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PptDocument::getTenantId, tenantId)
                .eq(PptDocument::getDeleted, 0)
                .orderByDesc(PptDocument::getCreatedAt);
        List<PptDocument> documents = pptDocumentMapper.selectList(wrapper);
        return documents.stream()
                .map(d -> toResponse(d, parseSlides(d.getContentJson())))
                .collect(java.util.stream.Collectors.toList());
    }

    public PptResponse getById(Long id) {
        PptDocument document = pptDocumentMapper.selectById(id);
        if (document == null || document.getDeleted() == 1) {
            return null;
        }
        return toResponse(document, parseSlides(document.getContentJson()));
    }

    @Transactional
    public void delete(Long id) {
        PptDocument document = pptDocumentMapper.selectById(id);
        if (document != null) {
            document.setDeleted(1);
            pptDocumentMapper.updateById(document);
            log.info("删除PPT文档: id={}", id);
        }
    }

    private List<PptSlideResponse> parseSlides(String contentJson) {
        if (contentJson == null || contentJson.isEmpty()) {
            return new ArrayList<>();
        }
        JSONArray array = JSON.parseArray(contentJson);
        List<PptSlideResponse> slides = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            PptSlideResponse slide = new PptSlideResponse();
            slide.setPageIndex(obj.getInteger("pageIndex"));
            slide.setTitle(obj.getString("title"));
            slide.setContent(obj.getString("content"));
            if (obj.containsKey("questionIds")) {
                slide.setQuestionIds(obj.getList("questionIds", Long.class));
            }
            slides.add(slide);
        }
        return slides;
    }

    private PptResponse toResponse(PptDocument document, List<PptSlideResponse> slides) {
        PptResponse response = new PptResponse();
        response.setId(document.getId());
        response.setTenantId(document.getTenantId());
        response.setTitle(document.getTitle());
        response.setSubject(document.getSubject());
        response.setTemplateType(document.getTemplateType());
        response.setTemplateUrl(document.getFilePath());
        response.setPageCount(document.getPageCount());
        response.setSlides(slides);
        response.setCreatedBy(document.getCreatedBy());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }
}