package com.edu.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI题目生成响应
 */
@Data
public class QuestionGenerateResponse {

    /**
     * 生成的题目列表
     */
    private List<GeneratedQuestion> questions;

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

    @Data
    public static class GeneratedQuestion {
        private String content;
        private String questionType;
        private Integer difficulty;
        private String options;  // JSON格式选项
        private String answer;
        private String answerAnalysis;
        private String knowledgePoints;  // JSON格式
    }
}