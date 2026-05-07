package com.edu.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    private Long id;
    private Long bankId;
    private String subject;
    private String questionType;  // CHOICE/FILL/JUDGE/CALCULATION
    private Integer difficulty;  // 1-简单, 2-中等, 3-困难
    private String content;  // 题目内容
    private String options;  // JSON格式选项（选择题）
    private String answer;  // 标准答案
    private String answerAnalysis;  // 答案解析
    private String knowledgePoints;  // JSON格式知识点标签
    private String source;  // MANUAL/AI_GENERATED
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
