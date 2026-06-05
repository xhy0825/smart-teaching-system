package com.edu.ai.client;

import com.edu.ai.entity.AIModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 客户端工厂
 * 根据配置动态创建对应的 AIClient
 */
@Slf4j
@Component
public class AIClientFactory {

    /**
     * 根据配置创建 AIClient
     */
    public AIClient createClient(AIModelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }

        String provider = config.getProvider();
        String apiKey = config.getApiKey();
        String apiUrl = config.getApiUrl();
        String model = config.getModel();
        Integer maxTokens = config.getMaxTokens() != null ? config.getMaxTokens() : 2000;
        Double temperature = config.getTemperature() != null ?
                config.getTemperature().doubleValue() : 0.7;

        if ("CLAUDE".equalsIgnoreCase(provider)) {
            log.debug("创建 Claude 客户端: model={}", model);
            return new ClaudeClient(apiKey, apiUrl, model, maxTokens);
        } else if (isOpenAICompatible(provider)) {
            log.debug("创建 OpenAI 兼容客户端: provider={}, model={}", provider, model);
            return new OpenAICompatibleClient(apiUrl, apiKey, model, maxTokens, temperature);
        } else {
            throw new IllegalArgumentException("不支持的供应商：" + provider);
        }
    }

    /**
     * 判断是否为 OpenAI 兼容格式
     */
    public boolean isOpenAICompatible(String provider) {
        return "DEEPSEEK".equalsIgnoreCase(provider) ||
               "OPENAI".equalsIgnoreCase(provider) ||
               "QWEN".equalsIgnoreCase(provider) ||
               "GLM".equalsIgnoreCase(provider);
    }
}
