package com.edu.common.config;

import com.edu.ai.client.ClaudeAPIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块配置类
 */
@Slf4j
@Configuration
public class AiConfig {

    @Value("${ai.cloud.api-key:}")
    private String apiKey;

    @Value("${ai.cloud.api-url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    @Value("${ai.cloud.model:claude-sonnet-4-6}")
    private String model;

    @Value("${ai.cloud.max-tokens:2000}")
    private Integer maxTokens;

    @Bean
    public ClaudeAPIClient claudeAPIClient() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Claude API Key 未配置，AI 功能将不可用");
            // 使用空 key 创建客户端，应用可正常启动
            return new ClaudeAPIClient("", apiUrl, model, maxTokens);
        }
        return new ClaudeAPIClient(apiKey, apiUrl, model, maxTokens);
    }
}
