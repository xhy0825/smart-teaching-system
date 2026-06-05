package com.edu.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AI 对话管理服务（内存存储）
 * 管理 AI 助教对话上下文，支持长对话
 * 适用于无 Redis 环境（使用 ConcurrentHashMap 替代）
 */
@Slf4j
@Service
public class ConversationService {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final long EXPIRE_HOURS = 24; // 对话上下文过期时间（小时）

    /**
     * 保存对话上下文
     */
    public void saveContext(String conversationId, String context) {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new IllegalArgumentException("conversationId cannot be null or empty");
        }
        cache.put(conversationId, new CacheEntry(context, System.currentTimeMillis()));
        log.info("保存对话上下文: conversationId={}", conversationId);
    }

    /**
     * 获取对话上下文
     */
    public String getContext(String conversationId) {
        CacheEntry entry = cache.get(conversationId);
        if (entry == null) {
            return null;
        }
        // 检查过期
        if (isExpired(entry)) {
            cache.remove(conversationId);
            return null;
        }
        // 刷新过期时间
        entry.timestamp = System.currentTimeMillis();
        return entry.context;
    }

    /**
     * 追加消息到对话上下文
     */
    public void appendMessage(String conversationId, String message) {
        String context = getContext(conversationId);
        if (context == null) {
            context = "[" + message + "]";
        } else {
            context = context.substring(0, context.length() - 1) + "," + message + "]";
        }
        saveContext(conversationId, context);
        log.info("追加消息到对话: conversationId={}", conversationId);
    }

    /**
     * 清除对话上下文
     */
    public void clearContext(String conversationId) {
        cache.remove(conversationId);
        log.info("清除对话上下文: conversationId={}", conversationId);
    }

    /**
     * 检查对话是否存在
     */
    public boolean hasContext(String conversationId) {
        CacheEntry entry = cache.get(conversationId);
        if (entry == null) {
            return false;
        }
        if (isExpired(entry)) {
            cache.remove(conversationId);
            return false;
        }
        return true;
    }

    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.timestamp > TimeUnit.HOURS.toMillis(EXPIRE_HOURS);
    }

    private static class CacheEntry {
        final String context;
        volatile long timestamp;

        CacheEntry(String context, long timestamp) {
            this.context = context;
            this.timestamp = timestamp;
        }
    }
}
