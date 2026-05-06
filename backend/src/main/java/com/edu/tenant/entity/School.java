package com.edu.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("school")
public class School extends BaseEntity {
    private Long id;
    private String name;
    private String address;
    private String contactPhone;
}
