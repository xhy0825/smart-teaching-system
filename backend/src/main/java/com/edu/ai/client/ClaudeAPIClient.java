package com.edu.ai.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Claude API 客户端（公共调用层）
 * 提取自 CloudAIProvider，统一处理 Claude API 调用
 */
@Slf4j
public class ClaudeAPIClient {

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final Integer maxTokens;
    private final RestTemplate restTemplate;
    private final AtomicLong callCount;
    private final AtomicLong tokenCount;

    public ClaudeAPIClient(String apiKey, String apiUrl, String model, Integer maxTokens) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.maxTokens = maxTokens;
        this.restTemplate = new RestTemplate();
        this.callCount = new AtomicLong(0);
        this.tokenCount = new AtomicLong(0);
    }

    /**
     * 调用 Claude API（纯文本 prompt）
     */
    public String call(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Claude API Key 未配置");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", maxTokens);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        body.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

        callCount.incrementAndGet();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);

        String responseBody = responseEntity.getBody();
        JSONObject responseJson = JSONObject.parseObject(responseBody);

        // 提取内容和 Token 统计
        JSONArray content = responseJson.getJSONArray("content");
        if (content != null && !content.isEmpty()) {
            JSONObject firstContent = content.getJSONObject(0);
            String text = firstContent.getString("text");

            // Token 统计
            JSONObject usage = responseJson.getJSONObject("usage");
            if (usage != null) {
                tokenCount.addAndGet(usage.getIntValue("total_tokens"));
            }

            return text;
        }

        throw new RuntimeException("AI 响应解析失败");
    }

    /**
     * 调用 Claude Vision API（支持图片）
     * @param prompt 文本提示词
     * @param imageBase64 Base64 编码的图片（可选）
     */
    public String callWithVision(String prompt, String imageBase64) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Claude API Key 未配置");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", maxTokens);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray contentArray = new JSONArray();

        // 文本部分
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        contentArray.add(textContent);

        // 图片部分（如果提供）
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image");
            JSONObject source = new JSONObject();
            source.put("type", "base64");
            source.put("media_type", "image/jpeg");
            source.put("data", imageBase64);
            imageContent.put("source", source);
            contentArray.add(imageContent);
        }

        message.put("content", contentArray);
        messages.add(message);
        body.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

        callCount.incrementAndGet();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);

        String responseBody = responseEntity.getBody();
        JSONObject responseJson = JSONObject.parseObject(responseBody);

        // 提取内容和 Token 统计
        JSONArray content = responseJson.getJSONArray("content");
        if (content != null && !content.isEmpty()) {
            JSONObject firstContent = content.getJSONObject(0);
            String text = firstContent.getString("text");

            // Token 统计
            JSONObject usage = responseJson.getJSONObject("usage");
            if (usage != null) {
                tokenCount.addAndGet(usage.getIntValue("total_tokens"));
            }

            return text;
        }

        throw new RuntimeException("AI 响应解析失败");
    }

    public long getCallCount() {
        return callCount.get();
    }

    public long getTokenCount() {
        return tokenCount.get();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
