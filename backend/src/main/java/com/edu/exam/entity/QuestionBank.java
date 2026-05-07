package com.edu.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_bank")
public class QuestionBank extends BaseEntity {
    private Long id;
    private String name;
    private String subject;  // MATH/PHYSICS/CHEMISTRY/ENGLISH
    private Integer gradeLevel;  // 适用年级段
    private String description;
    private Integer isPublic;  // 0-私有, 1-租户内公开
    private Long createdBy;
}
