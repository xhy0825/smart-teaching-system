package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.TenantNoBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission")
public class Permission extends TenantNoBaseEntity {
    private Long id;
    private String code;
    private String name;
    private String resource;
    private String action;
}
