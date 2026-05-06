package com.edu.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant")
public class Tenant extends TenantNoBaseEntity {
    private Long id;
    private String name;
    private String code;
    private String aiProvider;
    private String aiConfig;
    private Integer status;
    private LocalDate expireDate;
}
