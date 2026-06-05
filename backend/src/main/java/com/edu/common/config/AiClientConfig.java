package com.edu.common.config;

import com.edu.ai.client.AIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 客户端 Bean 配置
 * 提供默认的 AIClient Bean（降级模式），适用于 H2 无数据库配置环境
 */
@Slf4j
@Configuration
public class AiClientConfig {

    @Bean
    public AIClient defaultAiClient() {
        log.warn("AI 客户端未配置（降级模式），AI 功能将不可用");
        return new AIClient() {
            @Override
            public String chat(String prompt) {
                log.warn("AI 服务未配置，无法处理对话");
                return "AI 服务未配置，请在系统设置中配置大模型";
            }

            @Override
            public String chatWithVision(String prompt, String imageBase64) {
                log.warn("AI 服务未配置（Vision），无法处理图片识别");
                return "{\"score\":0,\"isCorrect\":0,\"accuracy\":0,\"analysis\":\"AI服务未配置\"}";
            }

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public long getCallCount() {
                return 0;
            }

            @Override
            public long getTokenCount() {
                return 0;
            }
        };
    }
}
