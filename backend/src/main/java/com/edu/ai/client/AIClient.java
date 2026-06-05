package com.edu.ai.client;

/**
 * AI 客户端接口
 * 统一不同供应商的调用方式
 */
public interface AIClient {

    /**
     * 纯文本对话
     * @param prompt 用户输入
     * @return AI 响应文本
     */
    String chat(String prompt);

    /**
     * 支持图片的对话（Vision）
     * @param prompt 文本提示词
     * @param imageBase64 Base64 编码的图片（可选）
     * @return AI 响应文本
     */
    String chatWithVision(String prompt, String imageBase64);

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();

    /**
     * 获取调用次数统计
     */
    long getCallCount();

    /**
     * 获取 Token 消耗统计
     */
    long getTokenCount();
}
