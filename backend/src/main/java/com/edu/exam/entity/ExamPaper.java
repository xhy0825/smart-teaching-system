package com.edu.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_paper")
public class ExamPaper extends BaseEntity {

    private Long id;
    private Long templateId;
    private String title;
    private String subject;
    private Long gradeId;
    private Long classId;
    private BigDecimal totalScore;
    private Integer timeLimit;
    private Integer status;
    private Long createdBy;
    private LocalDateTime publishedAt;
}
