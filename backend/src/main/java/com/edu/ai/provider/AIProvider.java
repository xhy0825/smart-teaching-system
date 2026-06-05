package com.edu.ai.provider;

import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import com.edu.ai.dto.QuestionGenerateRequest;
import com.edu.ai.dto.QuestionGenerateResponse;

/**
 * AI服务提供商接口
 */
public interface AIProvider {

    /**
     * 获取提供商名称
     */
    String getName();

    /**
     * 生成题目
     */
    QuestionGenerateResponse generateQuestions(QuestionGenerateRequest request);

    /**
     * 批改主观题
     */
    GradingResponse gradeSubjectiveQuestion(GradingRequest request);

    /**
     * AI 对话（智能助教）
     * @param prompt 用户输入
     * @return AI 响应
     */
    String chat(String prompt);

    /**
     * 检查服务状态
     */
    boolean isAvailable();

    /**
     * 获取调用次数统计
     */
    long getCallCount();

    /**
     * 获取Token消耗统计
     */
    long getTokenCount();
}