package com.edu.tenant.controller;

import com.edu.common.entity.Result;
import com.edu.common.util.TenantContextHolder;
import com.edu.tenant.entity.School;
import com.edu.tenant.service.SchoolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    public Result<School> createSchool(@Valid @RequestBody SchoolCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }
        School school = new School();
        school.setTenantId(tenantId);
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());
        School created = schoolService.createSchool(school);
        return Result.success(created);
    }

    @GetMapping
    public Result<List<School>> listSchools() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }
        List<School> schools = schoolService.listByTenant(tenantId);
        return Result.success(schools);
    }

    @GetMapping("/default")
    public Result<School> getDefaultSchool() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.error(403, "租户上下文缺失");
        }
        School school = schoolService.getDefaultSchool(tenantId);
        return Result.success(school);
    }

    @Data
    public static class SchoolCreateRequest {
        @NotBlank(message = "学校名称不能为空")
        private String name;
        private String address;
        private String contactPhone;
    }
}
