package com.edu.ai.controller;

import com.edu.ai.client.ClaudeAPIClient;
import com.edu.ai.service.ConversationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能助手控制器
 * 提供 AI 助教对话接口
 */
@Controller
@RequestMapping("/api/ai-tutor")
public class TutorController {

    private final ClaudeAPIClient claudeAPIClient;
    private final ConversationService conversationService;

    public TutorController(ClaudeAPIClient claudeAPIClient, ConversationService conversationService) {
        this.claudeAPIClient = claudeAPIClient;
        this.conversationService = conversationService;
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

        // 构建提示词
        String prompt = buildTutorPrompt(message);

        try {
            // 调用 Claude API
            String response = claudeAPIClient.call(prompt);

            // 保存对话上下文
            conversationService.appendMessage(conversationId,
                    "{\"role\":\"user\",\"content\":\"" + message + "\"}");
            conversationService.appendMessage(conversationId,
                    "{\"role\":\"assistant\",\"content\":\"" + response + "\"}");

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
