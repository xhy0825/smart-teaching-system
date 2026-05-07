package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
