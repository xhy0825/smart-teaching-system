package com.edu.grading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生错题记录实体
 */
@Data
@TableName("student_wrong_question")
public class StudentWrongQuestion {

    private Long id;

    private Long studentId;

    private Long questionId;

    private Long examPaperId;  // 来源试卷

    private Integer wrongCount;  // 错误次数

    private LocalDateTime lastWrongAt;  // 最近错误时间

    private LocalDateTime correctedAt;  // 纠错时间

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}