package com.edu.ai.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClaudeAPIClient 边界测试
 */
public class ClaudeAPIClientTest {

    private ClaudeAPIClient client;

    @BeforeEach
    void setUp() {
        // 使用测试值初始化
        client = new ClaudeAPIClient("test-api-key", "https://api.test.com/v1/messages", "claude-sonnet-4-6", 2000);
    }

    @Test
    void testConstructor_NullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClaudeAPIClient(null, "url", "model", 2000);
        });
    }

    @Test
    void testConstructor_EmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClaudeAPIClient("", "url", "model", 2000);
        });
    }

    @Test
    void testIsAvailable_ValidKey() {
        assertTrue(client.isAvailable());
    }

    @Test
    void testIsAvailable_InvalidKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ClaudeAPIClient("", "url", "model", 2000);
        });
    }

    @Test
    void testGetCallCount_InitialZero() {
        assertEquals(0, client.getCallCount());
    }

    @Test
    void testGetTokenCount_InitialZero() {
        assertEquals(0, client.getTokenCount());
    }

    // 注意：call() 和 callWithVision() 需要 mock RestTemplate 才能测试
    // 这里主要测试边界条件（参数验证、状态检查）
}
