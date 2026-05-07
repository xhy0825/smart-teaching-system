package com.edu.ai.dto;

import lombok.Data;

/**
 * AI批改请求
 */
@Data
public class GradingRequest {

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 题型
     */
    private String questionType;

    /**
     * 标准答案
     */
    private String correctAnswer;

    /**
     * 学生答案
     */
    private String studentAnswer;

    /**
     * 满分
     */
    private Double maxScore;

    /**
     * 是否需要详细分析
     */
    private boolean needAnalysis;
}