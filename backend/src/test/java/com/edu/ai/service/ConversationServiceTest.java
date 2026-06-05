package com.edu.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConversationService 边界测试（内存存储版）
 */
public class ConversationServiceTest {

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService();
    }

    @Test
    void saveContext_ValidInput() {
        conversationService.saveContext("conv1", "{\"messages\":[]}");
        String context = conversationService.getContext("conv1");
        assertEquals("{\"messages\":[]}", context);
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
        String context = conversationService.getContext("conv1");
        assertEquals("", context);
    }

    @Test
    void getContext_Exists() {
        conversationService.saveContext("conv1", "{\"messages\":[]}");
        String context = conversationService.getContext("conv1");
        assertEquals("{\"messages\":[]}", context);
    }

    @Test
    void getContext_NotExists() {
        String context = conversationService.getContext("conv1");
        assertNull(context);
    }

    @Test
    void appendMessage_NewConversation() {
        conversationService.appendMessage("conv1", "{\"role\":\"user\",\"content\":\"hello\"}");
        String context = conversationService.getContext("conv1");
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", context);
    }

    @Test
    void appendMessage_ExistingConversation() {
        conversationService.saveContext("conv1", "[{\"role\":\"user\",\"content\":\"hi\"}]");
        conversationService.appendMessage("conv1", "{\"role\":\"assistant\",\"content\":\"hello\"}");
        String context = conversationService.getContext("conv1");
        assertEquals("[{\"role\":\"user\",\"content\":\"hi\"},{\"role\":\"assistant\",\"content\":\"hello\"}]", context);
    }

    @Test
    void clearContext_ValidId() {
        conversationService.saveContext("conv1", "context");
        conversationService.clearContext("conv1");
        assertNull(conversationService.getContext("conv1"));
    }

    @Test
    void hasContext_Exists() {
        conversationService.saveContext("conv1", "context");
        assertTrue(conversationService.hasContext("conv1"));
    }

    @Test
    void hasContext_NotExists() {
        assertFalse(conversationService.hasContext("conv1"));
    }
}
