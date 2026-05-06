package com.edu.tenant.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TenantResponse {
    private Long id;
    private String name;
    private String code;
    private String aiProvider;
    private String aiConfig;
    private Integer status;
    private LocalDate expireDate;
    private LocalDateTime createdAt;
}
