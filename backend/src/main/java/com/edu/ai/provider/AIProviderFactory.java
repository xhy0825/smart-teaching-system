package com.edu.ai.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.edu.tenant.entity.Tenant;
import com.edu.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI Provider 工厂类
 * 根据租户配置动态选择AI提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIProviderFactory {

    private final CloudAIProvider cloudAIProvider;
    private final PrivateAIProvider privateAIProvider;
    private final TenantService tenantService;

    /**
     * 根据租户ID获取对应的AI Provider
     */
    public AIProvider getProvider(Long tenantId) {
        Tenant tenant = tenantService.getAndValidateTenant(tenantId);
        String aiProvider = tenant.getAiProvider();

        if (aiProvider == null || "CLOUD".equalsIgnoreCase(aiProvider)) {
            log.debug("租户{}使用云端AI服务", tenantId);
            return cloudAIProvider;
        }

        if ("PRIVATE".equalsIgnoreCase(aiProvider)) {
            log.debug("租户{}使用私有AI服务", tenantId);
            return privateAIProvider;
        }

        // 默认使用云端服务
        log.warn("租户{}的AI配置无效，默认使用云端服务", tenantId);
        return cloudAIProvider;
    }

    /**
     * 根据配置获取Provider
     */
    public AIProvider getProviderByConfig(String aiProvider, String aiConfig) {
        if (aiProvider == null || "CLOUD".equalsIgnoreCase(aiProvider)) {
            return cloudAIProvider;
        }

        if ("PRIVATE".equalsIgnoreCase(aiProvider)) {
            return privateAIProvider;
        }

        return cloudAIProvider;
    }

    /**
     * 获取默认Provider（云端）
     */
    public AIProvider getDefaultProvider() {
        return cloudAIProvider;
    }
}