package com.edu.ai.controller;

import com.edu.ai.entity.AIModelConfig;
import com.edu.ai.service.AIModelConfigService;
import com.edu.common.entity.Result;
import com.edu.common.util.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 模型配置 Controller
 * 提供模型配置的 CRUD 接口
 */
@RestController
@RequestMapping("/api/ai/configs")
public class AIModelConfigController {

    private final AIModelConfigService modelConfigService;

    public AIModelConfigController(AIModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取供应商预设
     */
    @GetMapping("/presets")
    public Result<List<Map<String, Object>>> getPresets() {
        return Result.success(modelConfigService.getProviderPresets());
    }

    /**
     * 列出租户所有配置
     * 从 JWT token 获取租户ID，无需前端传参
     */
    @GetMapping
    public Result<List<AIModelConfig>> list() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        List<AIModelConfig> configs = modelConfigService.listByTenant(tenantId);

        // 脱敏 API Key
        configs.forEach(config -> {
            if (config.getApiKey() != null && config.getApiKey().length() > 8) {
                String masked = config.getApiKey().substring(0, 4) + "****" + config.getApiKey().substring(config.getApiKey().length() - 4);
                config.setApiKey(masked);
            }
        });

        return Result.success(configs);
    }

    /**
     * 新增配置
     */
    @PostMapping
    public Result<AIModelConfig> save(@RequestBody AIModelConfig config) {
        // 从 JWT token 获取租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            config.setTenantId(tenantId);
        } else if (config.getTenantId() == null) {
            config.setTenantId(0L); // 默认系统配置
        }
        AIModelConfig saved = modelConfigService.saveConfig(config);
        return Result.success(saved);
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<AIModelConfig> update(@PathVariable Long id, @RequestBody AIModelConfig config) {
        // 从 JWT token 获取租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            config.setTenantId(tenantId);
        }
        AIModelConfig updated = modelConfigService.updateConfig(id, config);
        return Result.success(updated);
    }

    /**
     * 删除配置
     * 从 JWT token 获取租户ID
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        modelConfigService.deleteConfig(id, tenantId);
        return Result.success();
    }

    /**
     * 设为默认配置
     * 从 JWT token 获取租户ID
     */
    @PostMapping("/{id}/set-default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        modelConfigService.setDefault(id, tenantId);
        return Result.success();
    }

    /**
     * 测试连接
     */
    @PostMapping("/test")
    public Result<Map<String, Object>> testConnection(@RequestBody AIModelConfig config) {
        return Result.success(modelConfigService.testConnection(config));
    }
}
