package com.edu.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI题目生成请求
 */
@Data
public class QuestionGenerateRequest {

    /**
     * 学科: MATH/PHYSICS/CHEMISTRY/ENGLISH
     */
    private String subject;

    /**
     * 题型: CHOICE/FILL/JUDGE/CALCULATION
     */
    private String questionType;

    /**
     * 难度: 1-简单, 2-中等, 3-困难
     */
    private Integer difficulty;

    /**
     * 知识点
     */
    private String knowledgePoint;

    /**
     * 生成数量
     */
    private Integer count;

    /**
     * 额外要求
     */
    private String additionalRequirements;
}