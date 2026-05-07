package com.edu.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI批改响应
 */
@Data
public class GradingResponse {

    /**
     * AI评分
     */
    private BigDecimal score;

    /**
     * 是否正确: 0-错误, 1-正确, 2-部分正确
     */
    private Integer isCorrect;

    /**
     * 批改分析
     */
    private String analysis;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * Token消耗
     */
    private Integer tokensUsed;
}