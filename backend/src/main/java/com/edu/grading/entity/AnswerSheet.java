package com.edu.grading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题卡实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("answer_sheet")
public class AnswerSheet extends BaseEntity {

    private Long id;

    private Long examPaperId;

    private Long studentId;

    private Integer status;  // 0-未提交, 1-已提交, 2-批改中, 3-已批改

    private BigDecimal totalScore;

    private LocalDateTime submitTime;

    private LocalDateTime gradingTime;

    private Long gradedBy;  // 批改人ID
}