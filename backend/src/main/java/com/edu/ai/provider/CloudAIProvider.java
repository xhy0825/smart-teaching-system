package com.edu.ai.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.edu.ai.client.ClaudeAPIClient;
import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import com.edu.ai.dto.QuestionGenerateRequest;
import com.edu.ai.dto.QuestionGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 云端AI提供商实现（Claude/GPT）
 * 已重构：使用 ClaudeAPIClient 统一处理 API 调用
 */
@Slf4j
@Component
public class CloudAIProvider implements AIProvider {

    @Value("${ai.cloud.provider:Claude}")
    private String provider;

    @Value("${ai.cloud.api-url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    @Value("${ai.cloud.api-key:}")
    private String apiKey;

    @Value("${ai.cloud.model:claude-sonnet-4-6}")
    private String model;

    @Value("${ai.cloud.max-tokens:2000}")
    private Integer maxTokens;

    private ClaudeAPIClient claudeAPIClient;

    @PostConstruct
    public void init() {
        this.claudeAPIClient = new ClaudeAPIClient(apiKey, apiUrl, model, maxTokens);
    }

    @Override
    public String getName() {
        return "Cloud-" + provider;
    }

    @Override
    public QuestionGenerateResponse generateQuestions(QuestionGenerateRequest request) {
        QuestionGenerateResponse response = new QuestionGenerateResponse();
        response.setSuccess(false);

        if (!claudeAPIClient.isAvailable()) {
            response.setErrorMessage("AI服务未配置或不可用");
            return response;
        }

        try {
            String prompt = buildGeneratePrompt(request);
            String result = claudeAPIClient.call(prompt);

            // 解析结果
            List<QuestionGenerateResponse.GeneratedQuestion> questions = parseGeneratedQuestions(result);
            response.setQuestions(questions);
            response.setSuccess(true);
            log.info("云端AI生成题目成功: count={}", questions.size());

        } catch (Exception e) {
            log.error("云端AI生成题目失败: {}", e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public GradingResponse gradeSubjectiveQuestion(GradingRequest request) {
        GradingResponse response = new GradingResponse();
        response.setSuccess(false);

        if (!claudeAPIClient.isAvailable()) {
            response.setErrorMessage("AI服务未配置或不可用");
            return response;
        }

        try {
            String prompt = buildGradingPrompt(request);
            String result = claudeAPIClient.call(prompt);

            // 解析结果
            parseGradingResult(result, response);
            response.setSuccess(true);
            log.info("云端AI批改成功: score={}", response.getScore());

        } catch (Exception e) {
            log.error("云端AI批改失败: {}", e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public boolean isAvailable() {
        return claudeAPIClient.isAvailable();
    }

    @Override
    public long getCallCount() {
        return claudeAPIClient.getCallCount();
    }

    @Override
    public long getTokenCount() {
        return claudeAPIClient.getTokenCount();
    }

    private String buildGeneratePrompt(QuestionGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成").append(request.getCount()).append("道").append(getSubjectName(request.getSubject()));
        prompt.append(getTypeName(request.getQuestionType())).append("，难度为").append(getDifficultyName(request.getDifficulty())).append("。\n");

        if (request.getKnowledgePoint() != null) {
            prompt.append("知识点范围：").append(request.getKnowledgePoint()).append("\n");
        }

        if (request.getAdditionalRequirements() != null) {
            prompt.append("额外要求：").append(request.getAdditionalRequirements()).append("\n");
        }

        prompt.append("\n请按以下JSON格式返回：\n");
        prompt.append("[{\"content\":\"题目内容\",\"questionType\":\"题型\",\"difficulty\":难度数字,\"options\":\"选项JSON\",\"answer\":\"答案\",\"answerAnalysis\":\"解析\",\"knowledgePoints\":\"知识点JSON\"}]\n");
        prompt.append("只返回JSON数组，不要有其他文字。");

        return prompt.toString();
    }

    private String buildGradingPrompt(GradingRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请批改以下").append(getTypeName(request.getQuestionType())).append("：\n\n");
        prompt.append("题目：").append(request.getQuestionContent()).append("\n");
        prompt.append("标准答案：").append(request.getCorrectAnswer()).append("\n");
        prompt.append("学生答案：").append(request.getStudentAnswer()).append("\n");
        prompt.append("满分：").append(request.getMaxScore()).append("\n\n");

        prompt.append("请按以下JSON格式返回批改结果：\n");
        prompt.append("{\"score\":得分数字,\"isCorrect\":0或1或2,\"analysis\":\"批改分析\"}\n");
        prompt.append("isCorrect: 0表示错误，1表示正确，2表示部分正确。只返回JSON，不要有其他文字。");

        return prompt.toString();
    }

    private List<QuestionGenerateResponse.GeneratedQuestion> parseGeneratedQuestions(String result) {
        List<QuestionGenerateResponse.GeneratedQuestion> questions = new ArrayList<>();

        try {
            JSONArray jsonArray = JSON.parseArray(result);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                QuestionGenerateResponse.GeneratedQuestion q = new QuestionGenerateResponse.GeneratedQuestion();
                q.setContent(obj.getString("content"));
                q.setQuestionType(obj.getString("questionType"));
                q.setDifficulty(obj.getInteger("difficulty"));
                q.setOptions(obj.getString("options"));
                q.setAnswer(obj.getString("answer"));
                q.setAnswerAnalysis(obj.getString("answerAnalysis"));
                q.setKnowledgePoints(obj.getString("knowledgePoints"));
                questions.add(q);
            }
        } catch (Exception e) {
            log.warn("解析AI生成结果失败: {}", e.getMessage());
        }

        return questions;
    }

    private void parseGradingResult(String result, GradingResponse response) {
        try {
            JSONObject obj = JSON.parseObject(result);
            response.setScore(obj.getBigDecimal("score"));
            response.setIsCorrect(obj.getInteger("isCorrect"));
            response.setAnalysis(obj.getString("analysis"));
        } catch (Exception e) {
            log.warn("解析AI批改结果失败: {}", e.getMessage());
            response.setScore(BigDecimal.ZERO);
            response.setIsCorrect(0);
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

    private String getTypeName(String type) {
        switch (type) {
            case "CHOICE": return "选择题";
            case "FILL": return "填空题";
            case "JUDGE": return "判断题";
            case "CALCULATION": return "计算题";
            default: return type;
        }
    }

    private String getDifficultyName(Integer difficulty) {
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "中等";
        }
    }
}
