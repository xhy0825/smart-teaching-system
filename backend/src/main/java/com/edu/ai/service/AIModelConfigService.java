package com.edu.ai.service;

import com.alibaba.fastjson2.JSON;
import com.edu.ai.client.AIClient;
import com.edu.ai.client.ClaudeClient;
import com.edu.ai.client.OpenAICompatibleClient;
import com.edu.ai.entity.AIModelConfig;
import com.edu.ai.mapper.AIModelConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 模型配置 Service
 */
@Slf4j
@Service
public class AIModelConfigService {

    private final AIModelConfigMapper modelConfigMapper;

    @Value("${ai.encryption-key:default-enc-key-2026}")
    private String encryptionKey;

    public AIModelConfigService(AIModelConfigMapper modelConfigMapper) {
        this.modelConfigMapper = modelConfigMapper;
    }

    /**
     * 列出租户所有配置
     */
    public List<AIModelConfig> listByTenant(Long tenantId) {
        return modelConfigMapper.selectByTenantId(tenantId);
    }

    /**
     * 获取租户默认配置
     */
    public AIModelConfig getDefaultConfig(Long tenantId) {
        return modelConfigMapper.selectDefaultByTenantId(tenantId);
    }

    /**
     * 保存配置
     */
    public AIModelConfig saveConfig(AIModelConfig config) {
        // 加密 API Key
        config.setApiKey(encrypt(config.getApiKey()));

        // 如果设为默认，先清除其他默认配置
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            modelConfigMapper.clearDefaultFlag(config.getTenantId());
        }

        modelConfigMapper.insert(config);
        return config;
    }

    /**
     * 更新配置
     */
    public AIModelConfig updateConfig(Long id, AIModelConfig config) {
        AIModelConfig existing = modelConfigMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new RuntimeException("配置不存在");
        }

        // 更新字段
        if (config.getProvider() != null) existing.setProvider(config.getProvider());
        if (config.getProviderName() != null) existing.setProviderName(config.getProviderName());
        if (config.getApiUrl() != null) existing.setApiUrl(config.getApiUrl());
        if (config.getModel() != null) existing.setModel(config.getModel());
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            existing.setApiKey(encrypt(config.getApiKey()));
        }
        if (config.getAvailableModels() != null) existing.setAvailableModels(config.getAvailableModels());
        if (config.getMaxTokens() != null) existing.setMaxTokens(config.getMaxTokens());
        if (config.getTemperature() != null) existing.setTemperature(config.getTemperature());
        if (config.getIsEnabled() != null) existing.setIsEnabled(config.getIsEnabled());

        // 如果设为默认，先清除其他默认配置
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            modelConfigMapper.clearDefaultFlag(existing.getTenantId());
        }

        modelConfigMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除配置
     */
    public void deleteConfig(Long id, Long tenantId) {
        AIModelConfig config = modelConfigMapper.selectById(id);
        if (config == null || !config.getTenantId().equals(tenantId)) {
            throw new RuntimeException("配置不存在或无权限");
        }
        config.setDeleted(1);
        modelConfigMapper.updateById(config);
    }

    /**
     * 设为默认配置
     */
    public void setDefault(Long id, Long tenantId) {
        AIModelConfig config = modelConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1 || !config.getTenantId().equals(tenantId)) {
            throw new RuntimeException("配置不存在或无权限");
        }

        // 清除其他默认配置
        modelConfigMapper.clearDefaultFlag(tenantId);

        // 设置默认
        modelConfigMapper.setDefault(id);
    }

    /**
     * 根据模型名称查询配置
     */
    public AIModelConfig findByModel(String model, Long tenantId) {
        return modelConfigMapper.selectByModel(model, tenantId);
    }

    /**
     * 测试连接
     */
    public Map<String, Object> testConnection(AIModelConfig config) {
        Map<String, Object> result = new HashMap<>();

        try {
            AIClient client = createClient(config);

            if (!client.isAvailable()) {
                result.put("success", false);
                result.put("message", "客户端不可用，请检查配置");
                return result;
            }

            // 发送测试消息
            String response = client.chat("你好，请回复'连接成功'以确认连接正常。");

            result.put("success", true);
            result.put("message", "连接成功");
            result.put("response", response);

        } catch (Exception e) {
            log.error("测试连接失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "连接失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取供应商预设
     */
    public List<Map<String, Object>> getProviderPresets() {
        List<Map<String, Object>> presets = new ArrayList<>();

        // Claude 预设
        presets.add(createPreset("CLAUDE", "Claude (Anthropic)",
                "https://api.anthropic.com/v1/messages",
                new String[]{"claude-sonnet-4-6", "claude-haiku-3.5", "claude-opus-3"},
                "x-api-key"));

        // DeepSeek 预设
        presets.add(createPreset("DEEPSEEK", "DeepSeek",
                "https://api.deepseek.com/v1",
                new String[]{"deepseek-chat", "deepseek-reasoner"},
                "Authorization: Bearer"));

        // OpenAI 预设
        presets.add(createPreset("OPENAI", "OpenAI",
                "https://api.openai.com/v1",
                new String[]{"gpt-4o", "gpt-4o-mini", "gpt-4-turbo"},
                "Authorization: Bearer"));

        // 通义千问预设
        presets.add(createPreset("QWEN", "通义千问 (Qwen)",
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                new String[]{"qwen-turbo", "qwen-plus", "qwen-max"},
                "Authorization: Bearer"));

        return presets;
    }

    private Map<String, Object> createPreset(String provider, String name, String apiUrl, String[] models, String authType) {
        Map<String, Object> preset = new HashMap<>();
        preset.put("provider", provider);
        preset.put("name", name);
        preset.put("apiUrl", apiUrl);
        preset.put("models", models);
        preset.put("authType", authType);
        return preset;
    }

    /**
     * 根据配置创建 AIClient
     */
    public AIClient createClient(AIModelConfig config) {
        String decryptedKey = decrypt(config.getApiKey());

        if ("CLAUDE".equalsIgnoreCase(config.getProvider())) {
            return new ClaudeClient(
                    decryptedKey,
                    config.getApiUrl(),
                    config.getModel(),
                    config.getMaxTokens()
            );
        } else {
            // OpenAI 兼容格式（DeepSeek、OpenAI、通义千问等）
            return new OpenAICompatibleClient(
                    config.getApiUrl(),
                    decryptedKey,
                    config.getModel(),
                    config.getMaxTokens(),
                    config.getTemperature() != null ? config.getTemperature().doubleValue() : 0.7
            );
        }
    }

    /**
     * 简单的加密（实际项目应使用更安全的加密方式，如 AES）
     */
    private String encrypt(String plainText) {
        if (plainText == null) return null;
        // 简单 Base64 编码（实际应使用 AES 加密）
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    /**
     * 解密
     */
    private String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        // 简单 Base64 解码（实际应使用 AES 解密）
        return new String(Base64.getDecoder().decode(encryptedText));
    }
}
