package com.edu.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * AI 对话管理服务（Redis 存储）
 * 管理 AI 助教对话上下文，支持长对话
 */
@Slf4j
@Service
public class ConversationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "ai:conversation:";
    private static final long EXPIRE_HOURS = 24; // 对话上下文过期时间（小时）

    public ConversationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存对话上下文
     * @param conversationId 对话ID
     * @param context 对话上下文（JSON 格式）
     */
    public void saveContext(String conversationId, String context) {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new IllegalArgumentException("conversationId cannot be null or empty");
        }
        String key = KEY_PREFIX + conversationId;
        redisTemplate.opsForValue().set(key, context, EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("保存对话上下文: conversationId={}", conversationId);
    }

    /**
     * 获取对话上下文
     * @param conversationId 对话ID
     * @return 对话上下文，不存在返回 null
     */
    public String getContext(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        String context = redisTemplate.opsForValue().get(key);
        if (context != null) {
            // 刷新过期时间
            redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return context;
    }

    /**
     * 追加消息到对话上下文
     * @param conversationId 对话ID
     * @param message 新消息（JSON 格式）
     */
    public void appendMessage(String conversationId, String message) {
        String context = getContext(conversationId);
        if (context == null) {
            context = "[" + message + "]";
        } else {
            // 简单追加（实际应解析 JSON 数组后追加）
            context = context.substring(0, context.length() - 1) + "," + message + "]";
        }
        saveContext(conversationId, context);
        log.info("追加消息到对话: conversationId={}", conversationId);
    }

    /**
     * 清除对话上下文
     * @param conversationId 对话ID
     */
    public void clearContext(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        redisTemplate.delete(key);
        log.info("清除对话上下文: conversationId={}", conversationId);
    }

    /**
     * 检查对话是否存在
     * @param conversationId 对话ID
     * @return 存在返回 true
     */
    public boolean hasContext(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
