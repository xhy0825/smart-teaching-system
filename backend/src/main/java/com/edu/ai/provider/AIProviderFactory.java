package com.edu.ai.provider;

import com.edu.ai.client.AIClient;
import com.edu.ai.client.AIClientFactory;
import com.edu.ai.entity.AIModelConfig;
import com.edu.ai.service.AIModelConfigService;
import com.edu.tenant.entity.Tenant;
import com.edu.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI Provider 工厂类
 * 根据租户配置动态选择AI提供商
 * 已重构：支持从数据库加载多供应商配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIProviderFactory {

    private final CloudAIProvider cloudAIProvider;
    private final PrivateAIProvider privateAIProvider;
    private final TenantService tenantService;
    private final AIModelConfigService modelConfigService;
    private final AIClientFactory clientFactory;

    /**
     * 根据租户ID获取对应的AI Provider
     */
    public AIProvider getProvider(Long tenantId) {
        Tenant tenant = tenantService.getAndValidateTenant(tenantId);

        // 尝试从数据库加载配置
        AIModelConfig config = modelConfigService.getDefaultConfig(tenantId);

        if (config != null) {
            log.debug("租户{}使用数据库配置: provider={}, model={}",
                    tenantId, config.getProvider(), config.getModel());

            // 如果是私有部署，返回 PrivateAIProvider
            if ("PRIVATE".equalsIgnoreCase(config.getProvider())) {
                return privateAIProvider;
            }

            // 云端配置：更新 CloudAIProvider 的配置
            cloudAIProvider.setConfig(config);
            return cloudAIProvider;
        }

        // 数据库无配置，降级到租户表的配置
        String aiProvider = tenant.getAiProvider();

        if (aiProvider == null || "CLOUD".equalsIgnoreCase(aiProvider)) {
            log.debug("租户{}使用云端AI服务（降级）", tenantId);
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
     * 根据配置获取Provider（用于测试等场景）
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

    /**
     * 根据 AIModelConfig 创建 AIClient
     */
    public AIClient createClient(AIModelConfig config) {
        return clientFactory.createClient(config);
    }
}
