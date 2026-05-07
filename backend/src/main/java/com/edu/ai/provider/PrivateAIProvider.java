package com.edu.ai.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import com.edu.ai.dto.QuestionGenerateRequest;
import com.edu.ai.dto.QuestionGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 私有部署AI提供商实现
 */
@Slf4j
@Component
public class PrivateAIProvider implements AIProvider {

    @Value("${ai.private.service-url:http://localhost:8081}")
    private String serviceUrl;

    @Value("${ai.private.timeout:30000}")
    private Integer timeout;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicLong tokenCount = new AtomicLong(0);

    @Override
    public String getName() {
        return "Private-AI";
    }

    @Override
    public QuestionGenerateResponse generateQuestions(QuestionGenerateRequest request) {
        QuestionGenerateResponse response = new QuestionGenerateResponse();
        response.setSuccess(false);

        if (!isAvailable()) {
            response.setErrorMessage("私有AI服务不可用");
            return response;
        }

        try {
            String url = serviceUrl + "/api/generate-questions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            body.put("subject", request.getSubject());
            body.put("questionType", request.getQuestionType());
            body.put("difficulty", request.getDifficulty());
            body.put("knowledgePoint", request.getKnowledgePoint());
            body.put("count", request.getCount());

            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

            callCount.incrementAndGet();

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            String responseBody = responseEntity.getBody();
            JSONObject responseJson = JSON.parseObject(responseBody);

            if (responseJson.getBooleanValue("success")) {
                JSONArray questionsArray = responseJson.getJSONArray("questions");
                List<QuestionGenerateResponse.GeneratedQuestion> questions = new ArrayList<>();

                for (int i = 0; i < questionsArray.size(); i++) {
                    JSONObject qObj = questionsArray.getJSONObject(i);
                    QuestionGenerateResponse.GeneratedQuestion q = new QuestionGenerateResponse.GeneratedQuestion();
                    q.setContent(qObj.getString("content"));
                    q.setQuestionType(qObj.getString("questionType"));
                    q.setDifficulty(qObj.getInteger("difficulty"));
                    q.setOptions(qObj.getString("options"));
                    q.setAnswer(qObj.getString("answer"));
                    q.setAnswerAnalysis(qObj.getString("answerAnalysis"));
                    q.setKnowledgePoints(qObj.getString("knowledgePoints"));
                    questions.add(q);
                }

                response.setQuestions(questions);
                response.setSuccess(true);

                // Token统计
                if (responseJson.containsKey("tokensUsed")) {
                    tokenCount.addAndGet(responseJson.getIntValue("tokensUsed"));
                    response.setTokensUsed(responseJson.getInteger("tokensUsed"));
                }

                log.info("私有AI生成题目成功: count={}", questions.size());
            } else {
                response.setErrorMessage(responseJson.getString("error"));
            }

        } catch (Exception e) {
            log.error("私有AI生成题目失败: {}", e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public GradingResponse gradeSubjectiveQuestion(GradingRequest request) {
        GradingResponse response = new GradingResponse();
        response.setSuccess(false);

        if (!isAvailable()) {
            response.setErrorMessage("私有AI服务不可用");
            return response;
        }

        try {
            String url = serviceUrl + "/api/grade";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            body.put("questionContent", request.getQuestionContent());
            body.put("questionType", request.getQuestionType());
            body.put("correctAnswer", request.getCorrectAnswer());
            body.put("studentAnswer", request.getStudentAnswer());
            body.put("maxScore", request.getMaxScore());
            body.put("needAnalysis", request.isNeedAnalysis());

            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

            callCount.incrementAndGet();

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            String responseBody = responseEntity.getBody();
            JSONObject responseJson = JSON.parseObject(responseBody);

            if (responseJson.getBooleanValue("success")) {
                response.setScore(responseJson.getBigDecimal("score"));
                response.setIsCorrect(responseJson.getInteger("isCorrect"));
                response.setAnalysis(responseJson.getString("analysis"));
                response.setSuccess(true);

                // Token统计
                if (responseJson.containsKey("tokensUsed")) {
                    tokenCount.addAndGet(responseJson.getIntValue("tokensUsed"));
                    response.setTokensUsed(responseJson.getInteger("tokensUsed"));
                }

                log.info("私有AI批改成功: score={}", response.getScore());
            } else {
                response.setErrorMessage(responseJson.getString("error"));
            }

        } catch (Exception e) {
            log.error("私有AI批改失败: {}", e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    serviceUrl + "/health", HttpMethod.GET, null, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("私有AI服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getCallCount() {
        return callCount.get();
    }

    @Override
    public long getTokenCount() {
        return tokenCount.get();
    }
}