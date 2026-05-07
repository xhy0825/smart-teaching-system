package com.edu.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {
    private Long id;
    private Long classId;
    private Long userId;  // 关联用户账号（可选）
    private String name;
    private String studentNo;
    private Integer gender;  // 1-男, 2-女
    private LocalDate birthDate;
    private Integer status;  // 1-在读, 0-离校
}
