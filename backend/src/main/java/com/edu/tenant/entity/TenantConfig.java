package com.edu.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_config")
public class TenantConfig extends TenantNoBaseEntity {
    private Long id;
    private Long tenantId;
    private String configKey;
    private String configValue;
}
