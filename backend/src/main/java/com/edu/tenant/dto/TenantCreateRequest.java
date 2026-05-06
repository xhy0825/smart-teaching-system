package com.edu.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantCreateRequest {
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称最长100字符")
    private String name;

    @NotBlank(message = "租户编码不能为空")
    @Size(max = 50, message = "租户编码最长50字符")
    private String code;

    private String aiProvider;
    private String aiConfig;
    private String expireDate;
}
