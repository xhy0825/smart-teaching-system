package com.edu.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 试卷模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_template")
public class ExamTemplate extends BaseEntity {

    private Long id;
    private String name;
    private String subject;
    private BigDecimal totalScore;
    private Integer timeLimit;
    private String structure;
    private Long createdBy;
}
