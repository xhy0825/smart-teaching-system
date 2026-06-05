package com.edu.ai.controller;

import com.edu.ai.provider.AIProvider;
import com.edu.ai.provider.AIProviderFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能助手控制器
 * 提供 AI 助教对话接口
 * 已重构：使用 AIProvider 接口，支持多供应商
 */
@Controller
@RequestMapping("/api/ai-tutor")
public class TutorController {

    private final AIProviderFactory providerFactory;

    public TutorController(AIProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * AI 助教对话（纯文本）
     */
    @PostMapping("/chat")
    @ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String message = request.get("message");

        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "conv_" + System.currentTimeMillis();
        }

        // 获取 AI Provider（使用默认租户 0）
        AIProvider provider = providerFactory.getDefaultProvider();

        if (!provider.isAvailable()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "AI 服务不可用，请检查配置");
            return result;
        }

        try {
            // 构建提示词
            String prompt = buildTutorPrompt(message);

            // 调用 AI（通过 AIProvider）
            String response = provider.chat(prompt);

            // 保存对话上下文（TODO: 实现 ConversationService）
            // conversationService.appendMessage(conversationId, ...);

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", conversationId);
            result.put("response", response);
            result.put("success", true);
            return result;

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 构建智能助手提示词
     */
    private String buildTutorPrompt(String userMessage) {
        return "你是教师的智能助教，帮助解答教学问题、提供备课建议、分析学生数据。\n\n" +
               "教师问题：" + userMessage + "\n\n" +
               "请用中文专业、简洁地回答，必要时提供具体建议。";
    }
}
