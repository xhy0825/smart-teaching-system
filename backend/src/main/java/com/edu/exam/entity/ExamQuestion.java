package com.edu.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷题目关联实体（无tenantId）
 */
@Data
@TableName("exam_question")
public class ExamQuestion {

    private Long id;
    private Long examPaperId;
    private Long questionId;
    private Integer sequence;
    private BigDecimal score;
    private LocalDateTime createdAt;
}
