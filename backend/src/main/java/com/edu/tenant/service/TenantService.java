package com.edu.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.exception.TenantException;
import com.edu.tenant.entity.Tenant;
import com.edu.tenant.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    @Transactional
    public Tenant createTenant(Tenant tenant) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getCode, tenant.getCode());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("租户编码已存在");
        }
        if (tenant.getStatus() == null) {
            tenant.setStatus(1);
        }
        if (tenant.getAiProvider() == null) {
            tenant.setAiProvider("CLOUD");
        }
        baseMapper.insert(tenant);
        log.info("创建租户成功: id={}, code={}", tenant.getId(), tenant.getCode());
        return tenant;
    }

    public Tenant getTenantByCode(String code) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    public Tenant getAndValidateTenant(Long tenantId) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        if (tenant.getStatus() == 0) {
            throw TenantException.disabled();
        }
        if (tenant.getExpireDate() != null && tenant.getExpireDate().isBefore(LocalDate.now())) {
            throw TenantException.expired();
        }
        return tenant;
    }

    @Transactional
    public void updateAIConfig(Long tenantId, String aiProvider, String aiConfig) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        tenant.setAiProvider(aiProvider);
        tenant.setAiConfig(aiConfig);
        baseMapper.updateById(tenant);
        log.info("更新租户AI配置: tenantId={}, provider={}", tenantId, aiProvider);
    }

    @Transactional
    public void disableTenant(Long tenantId) {
        Tenant tenant = baseMapper.selectById(tenantId);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        tenant.setStatus(0);
        baseMapper.updateById(tenant);
        log.info("禁用租户: tenantId={}", tenantId);
    }
}
