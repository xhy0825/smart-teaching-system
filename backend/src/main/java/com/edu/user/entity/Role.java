package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends TenantNoBaseEntity {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String description;
}
