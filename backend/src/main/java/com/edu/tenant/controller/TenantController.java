package com.edu.tenant.controller;

import com.edu.common.entity.Result;
import com.edu.tenant.dto.TenantCreateRequest;
import com.edu.tenant.dto.TenantResponse;
import com.edu.tenant.entity.Tenant;
import com.edu.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public Result<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCode(request.getCode());
        tenant.setAiProvider(request.getAiProvider() != null ? request.getAiProvider() : "CLOUD");
        tenant.setAiConfig(request.getAiConfig());
        if (request.getExpireDate() != null) {
            tenant.setExpireDate(LocalDate.parse(request.getExpireDate()));
        }
        Tenant created = tenantService.createTenant(tenant);
        return Result.success(toResponse(created));
    }

    @GetMapping("/code/{code}")
    public Result<TenantResponse> getTenantByCode(@PathVariable String code) {
        Tenant tenant = tenantService.getTenantByCode(code);
        if (tenant == null) {
            return Result.error("租户不存在");
        }
        return Result.success(toResponse(tenant));
    }

    @GetMapping("/{id}")
    public Result<TenantResponse> getTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.getAndValidateTenant(id);
        return Result.success(toResponse(tenant));
    }

    @PutMapping("/{id}/ai-config")
    public Result<Void> updateAIConfig(@PathVariable Long id,
                                        @RequestParam String provider,
                                        @RequestBody String config) {
        tenantService.updateAIConfig(id, provider, config);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disableTenant(@PathVariable Long id) {
        tenantService.disableTenant(id);
        return Result.success();
    }

    private TenantResponse toResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setCode(tenant.getCode());
        response.setAiProvider(tenant.getAiProvider());
        response.setAiConfig(tenant.getAiConfig());
        response.setStatus(tenant.getStatus());
        response.setExpireDate(tenant.getExpireDate());
        response.setCreatedAt(tenant.getCreatedAt());
        return response;
    }
}
