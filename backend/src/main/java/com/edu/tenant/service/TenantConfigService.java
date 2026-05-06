package com.edu.tenant.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.tenant.entity.TenantConfig;
import com.edu.tenant.mapper.TenantConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigService extends ServiceImpl<TenantConfigMapper, TenantConfig> {

    public JSONObject getAIConfig(Long tenantId) {
        TenantConfig config = getConfig(tenantId, "ai_config");
        if (config == null || config.getConfigValue() == null) {
            return new JSONObject();
        }
        return JSON.parseObject(config.getConfigValue());
    }

    public void setAIConfig(Long tenantId, JSONObject aiConfig) {
        setConfig(tenantId, "ai_config", aiConfig.toJSONString());
    }

    public TenantConfig getConfig(Long tenantId, String key) {
        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getTenantId, tenantId)
                .eq(TenantConfig::getConfigKey, key);
        return baseMapper.selectOne(wrapper);
    }

    public void setConfig(Long tenantId, String key, String value) {
        TenantConfig config = getConfig(tenantId, key);
        if (config == null) {
            config = new TenantConfig();
            config.setTenantId(tenantId);
            config.setConfigKey(key);
            config.setConfigValue(value);
            baseMapper.insert(config);
        } else {
            config.setConfigValue(value);
            baseMapper.updateById(config);
        }
        log.info("设置租户配置: tenantId={}, key={}", tenantId, key);
    }
}
