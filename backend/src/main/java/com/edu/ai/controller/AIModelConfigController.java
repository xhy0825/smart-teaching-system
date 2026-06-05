package com.edu.ai.controller;

import com.edu.ai.entity.AIModelConfig;
import com.edu.ai.service.AIModelConfigService;
import com.edu.common.util.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public Map<String, Object> getPresets() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", modelConfigService.getProviderPresets());
        return result;
    }

    /**
     * 列出租户所有配置
     * 从 JWT token 获取租户ID，无需前端传参
     */
    @GetMapping
    public Map<String, Object> list() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        Map<String, Object> result = new HashMap<>();
        List<AIModelConfig> configs = modelConfigService.listByTenant(tenantId);

        // 脱敏 API Key
        configs.forEach(config -> {
            if (config.getApiKey() != null && config.getApiKey().length() > 8) {
                String masked = config.getApiKey().substring(0, 4) + "****" + config.getApiKey().substring(config.getApiKey().length() - 4);
                config.setApiKey(masked);
            }
        });

        result.put("success", true);
        result.put("data", configs);
        return result;
    }

    /**
     * 新增配置
     */
    @PostMapping
    public Map<String, Object> save(@RequestBody AIModelConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 确保 tenantId 不为空
            if (config.getTenantId() == null) {
                config.setTenantId(0L); // 默认系统配置
            }

            AIModelConfig saved = modelConfigService.saveConfig(config);
            result.put("success", true);
            result.put("data", saved);
            result.put("message", "配置保存成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AIModelConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            AIModelConfig updated = modelConfigService.updateConfig(id, config);
            result.put("success", true);
            result.put("data", updated);
            result.put("message", "配置更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除配置
     * 从 JWT token 获取租户ID
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        Map<String, Object> result = new HashMap<>();
        try {
            modelConfigService.deleteConfig(id, tenantId);
            result.put("success", true);
            result.put("message", "配置删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 设为默认配置
     * 从 JWT token 获取租户ID
     */
    @PostMapping("/{id}/set-default")
    public Map<String, Object> setDefault(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("无法获取租户信息，请重新登录");
        }
        Map<String, Object> result = new HashMap<>();
        try {
            modelConfigService.setDefault(id, tenantId);
            result.put("success", true);
            result.put("message", "已设为默认配置");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 测试连接
     */
    @PostMapping("/test")
    public Map<String, Object> testConnection(@RequestBody AIModelConfig config) {
        return modelConfigService.testConnection(config);
    }
}
