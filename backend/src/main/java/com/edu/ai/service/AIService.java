package com.edu.ai.service;

import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import com.edu.ai.dto.QuestionGenerateRequest;
import com.edu.ai.dto.QuestionGenerateResponse;
import com.edu.ai.provider.AIProvider;
import com.edu.ai.provider.AIProviderFactory;
import com.edu.common.util.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI服务统一入口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIProviderFactory providerFactory;

    /**
     * 生成题目
     */
    public QuestionGenerateResponse generateQuestions(QuestionGenerateRequest request) {
        AIProvider provider = getProvider();
        log.info("使用{}生成题目: subject={}, type={}, count={}",
                provider.getName(), request.getSubject(), request.getQuestionType(), request.getCount());
        return provider.generateQuestions(request);
    }

    /**
     * 批改主观题
     */
    public GradingResponse gradeSubjectiveQuestion(GradingRequest request) {
        AIProvider provider = getProvider();
        log.info("使用{}批改题目: type={}, maxScore={}",
                provider.getName(), request.getQuestionType(), request.getMaxScore());
        return provider.gradeSubjectiveQuestion(request);
    }

    /**
     * 检查当前AI服务状态
     */
    public boolean checkStatus() {
        AIProvider provider = getProvider();
        return provider.isAvailable();
    }

    /**
     * 获取当前Provider名称
     */
    public String getCurrentProviderName() {
        return getProvider().getName();
    }

    /**
     * 获取调用统计
     */
    public AIUsageStats getUsageStats() {
        AIProvider provider = getProvider();
        AIUsageStats stats = new AIUsageStats();
        stats.setProviderName(provider.getName());
        stats.setCallCount(provider.getCallCount());
        stats.setTokenCount(provider.getTokenCount());
        stats.setAvailable(provider.isAvailable());
        return stats;
    }

    /**
     * 获取当前租户的AI Provider
     */
    private AIProvider getProvider() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            log.warn("租户上下文缺失，使用默认AI服务");
            return providerFactory.getDefaultProvider();
        }
        return providerFactory.getProvider(tenantId);
    }

    /**
     * AI使用统计
     */
    public static class AIUsageStats {
        private String providerName;
        private long callCount;
        private long tokenCount;
        private boolean available;

        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public long getCallCount() { return callCount; }
        public void setCallCount(long callCount) { this.callCount = callCount; }
        public long getTokenCount() { return tokenCount; }
        public void setTokenCount(long tokenCount) { this.tokenCount = tokenCount; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}