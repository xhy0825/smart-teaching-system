package com.edu.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConversationService 边界测试（简化版）
 */
public class ConversationServiceTest {

    private RedisTemplate<String, String> mockRedisTemplate;
    private ValueOperations<String, String> mockValueOps;
    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        mockRedisTemplate = mock(RedisTemplate.class);
        mockValueOps = mock(ValueOperations.class);
        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOps);
        conversationService = new ConversationService(mockRedisTemplate);
    }

    @Test
    void saveContext_ValidInput() {
        conversationService.saveContext("conv1", "{\"messages\":[]}");
        verify(mockValueOps).set(eq("ai:conversation:conv1"), eq("{\"messages\":[]}"), eq(24L), any());
    }

    @Test
    void saveContext_NullConversationId() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversationService.saveContext(null, "context");
        });
    }

    @Test
    void saveContext_EmptyConversationId() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversationService.saveContext("", "context");
        });
    }

    @Test
    void saveContext_EmptyContext() {
        conversationService.saveContext("conv1", "");
        verify(mockValueOps).set(eq("ai:conversation:conv1"), eq(""), eq(24L), any());
    }

    @Test
    void getContext_Exists() {
        when(mockValueOps.get("ai:conversation:conv1")).thenReturn("{\"messages\":[]}");
        String context = conversationService.getContext("conv1");
        assertEquals("{\"messages\":[]}", context);
    }

    @Test
    void getContext_NotExists() {
        when(mockValueOps.get(anyString())).thenReturn(null);
        String context = conversationService.getContext("conv1");
        assertNull(context);
    }

    @Test
    void appendMessage_NewConversation() {
        when(mockValueOps.get("ai:conversation:conv1")).thenReturn(null);
        conversationService.appendMessage("conv1", "{\"role\":\"user\",\"content\":\"hello\"}");
        verify(mockValueOps).set(eq("ai:conversation:conv1"), eq("[{\"role\":\"user\",\"content\":\"hello\"}]"), eq(24L), any());
    }

    @Test
    void appendMessage_ExistingConversation() {
        when(mockValueOps.get("ai:conversation:conv1")).thenReturn("[{\"role\":\"user\",\"content\":\"hi\"}]");
        conversationService.appendMessage("conv1", "{\"role\":\"assitant\",\"content\":\"hello\"}");
        verify(mockValueOps).set(eq("ai:conversation:conv1"), eq("[{\"role\":\"user\",\"content\":\"hi\"},{\"role\":\"assitant\",\"content\":\"hello\"}]"), eq(24L), any());
    }

    @Test
    void clearContext_ValidId() {
        conversationService.clearContext("conv1");
        verify(mockRedisTemplate).delete("ai:conversation:conv1");
    }

    @Test
    void hasContext_Exists() {
        when(mockRedisTemplate.hasKey(anyString())).thenReturn(true);
        assertTrue(conversationService.hasContext("conv1"));
    }

    @Test
    void hasContext_NotExists() {
        when(mockRedisTemplate.hasKey(anyString())).thenReturn(false);
        assertFalse(conversationService.hasContext("conv1"));
    }
}
