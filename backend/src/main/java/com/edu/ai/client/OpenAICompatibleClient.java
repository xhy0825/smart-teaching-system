package com.edu.ai.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicLong;

/**
 * OpenAI 兼容客户端
 * 支持 DeepSeek、OpenAI、通义千问等使用 OpenAI 格式 API 的供应商
 */
@Slf4j
public class OpenAICompatibleClient implements AIClient {

    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final Integer maxTokens;
    private final Double temperature;
    private final RestTemplate restTemplate;
    private final AtomicLong callCount;
    private final AtomicLong tokenCount;

    public OpenAICompatibleClient(String apiUrl, String apiKey, String model,
                                 Integer maxTokens, Double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        this.apiUrl = apiUrl.endsWith("/v1") ? apiUrl : apiUrl + "/v1";
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens != null ? maxTokens : 2000;
        this.temperature = temperature != null ? temperature : 0.7;
        this.restTemplate = new RestTemplate();
        this.callCount = new AtomicLong(0);
        this.tokenCount = new AtomicLong(0);
    }

    @Override
    public String chat(String prompt) {
        return doCall(prompt, null);
    }

    @Override
    public String chatWithVision(String prompt, String imageBase64) {
        // OpenAI 兼容 API 也支持 Vision（如果模型支持）
        return doCall(prompt, imageBase64);
    }

    private String doCall(String prompt, String imageBase64) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        body.put("stream", false);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            // Vision 模式：使用多内容格式
            JSONArray contentArray = new JSONArray();
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", prompt);
            contentArray.add(textContent);

            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", "data:image/jpeg;base64," + imageBase64);
            imageContent.put("image_url", imageUrlObj);
            contentArray.add(imageContent);

            message.put("content", contentArray);
        } else {
            // 纯文本模式
            message.put("content", prompt);
        }

        messages.add(message);
        body.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        callCount.incrementAndGet();

        // 确保 URL 指向 /v1/chat/completions
        String url = apiUrl;
        if (!url.contains("/chat/completions")) {
            url = url + "/chat/completions";
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        String responseBody = responseEntity.getBody();
        JSONObject responseJson = JSONObject.parseObject(responseBody);

        // 提取内容（OpenAI 兼容格式）
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject messageObj = firstChoice.getJSONObject("message");
            String content = messageObj.getString("content");

            // Token 统计
            JSONObject usage = responseJson.getJSONObject("usage");
            if (usage != null) {
                tokenCount.addAndGet(usage.getIntValue("total_tokens"));
            }

            return content;
        }

        throw new RuntimeException("AI 响应解析失败：" + responseBody);
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
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
