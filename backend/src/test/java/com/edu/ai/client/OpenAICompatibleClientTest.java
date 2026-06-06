package com.edu.ai.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OpenAICompatibleClient 单元测试
 * 测试通过 LiteLLM Proxy 调用 OpenAI 兼容格式 API
 */
@ExtendWith(MockitoExtension.class)
class OpenAICompatibleClientTest {

    @Mock
    private RestTemplate restTemplate;

    private OpenAICompatibleClient client;

    private static final String TEST_URL = "http://localhost:8000/v1";
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_MODEL = "gpt-4o";
    private static final Integer TEST_MAX_TOKENS = 2000;
    private static final Double TEST_TEMPERATURE = 0.7;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射创建 OpenAICompatibleClient，注入 mock 的 RestTemplate
        client = new OpenAICompatibleClient(TEST_URL, TEST_API_KEY, TEST_MODEL, TEST_MAX_TOKENS, TEST_TEMPERATURE);

        // 使用反射替换 RestTemplate 为 mock
        Field restTemplateField = OpenAICompatibleClient.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(client, restTemplate);
    }

    /**
     * 测试正常聊天调用
     */
    @Test
    void testChatSuccess() {
        // 模拟成功响应
        JSONObject content = new JSONObject();
        content.put("text", "这是 AI 的回复");

        JSONArray contentArray = new JSONArray();
        contentArray.add(content);

        JSONObject choice = new JSONObject();
        choice.put("message", new JSONObject().fluentPut("content", "这是 AI 的回复"));

        JSONArray choices = new JSONArray();
        choices.add(choice);

        JSONObject responseJson = new JSONObject();
        responseJson.put("choices", choices);
        responseJson.put("usage", new JSONObject().fluentPut("total_tokens", 100));

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                responseJson.toJSONString(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_URL + "/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 执行测试
        String result = client.chat("你好");

        // 验证结果
        assertNotNull(result);
        assertEquals("这是 AI 的回复", result);
        verify(restTemplate).exchange(
                eq(TEST_URL + "/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    /**
     * 测试 chatWithVision - 带图片的对话
     */
    @Test
    void testChatWithVisionSuccess() {
        // 模拟成功响应
        JSONObject choice = new JSONObject();
        choice.put("message", new JSONObject().fluentPut("content", "图片内容分析完成"));

        JSONArray choices = new JSONArray();
        choices.add(choice);

        JSONObject responseJson = new JSONObject();
        responseJson.put("choices", choices);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                responseJson.toJSONString(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_URL + "/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 执行测试
        String result = client.chatWithVision("描述这张图片", "base64encodedimage");

        // 验证结果
        assertNotNull(result);
        assertEquals("图片内容分析完成", result);
    }

    /**
     * 测试 Token 统计
     */
    @Test
    void testTokenCount() {
        // 模拟成功响应，包含 usage
        JSONObject choice = new JSONObject();
        choice.put("message", new JSONObject().fluentPut("content", "回复"));

        JSONArray choices = new JSONArray();
        choices.add(choice);

        JSONObject usage = new JSONObject();
        usage.put("total_tokens", 150);

        JSONObject responseJson = new JSONObject();
        responseJson.put("choices", choices);
        responseJson.put("usage", usage);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                responseJson.toJSONString(), HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 第一次调用
        client.chat("测试1");
        long tokenCount1 = client.getTokenCount();

        // 第二次调用
        client.chat("测试2");
        long tokenCount2 = client.getTokenCount();

        // 验证 Token 统计
        assertEquals(150, tokenCount1);
        assertEquals(300, tokenCount2);  // 两次调用累计
    }

    /**
     * 测试调用次数统计
     */
    @Test
    void testCallCount() {
        // 模拟响应
        JSONObject choice = new JSONObject();
        choice.put("message", new JSONObject().fluentPut("content", "回复"));

        JSONArray choices = new JSONArray();
        choices.add(choice);

        JSONObject responseJson = new JSONObject();
        responseJson.put("choices", choices);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                responseJson.toJSONString(), HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 初始次数
        assertEquals(0, client.getCallCount());

        // 第一次调用
        client.chat("测试1");
        assertEquals(1, client.getCallCount());

        // 第二次调用
        client.chat("测试2");
        assertEquals(2, client.getCallCount());
    }

    /**
     * 测试网络错误 - Proxy 不可用
     */
    @Test
    void testProxyUnavailable() {
        // 模拟网络异常
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        // 验证异常抛出
        assertThrows(RuntimeException.class, () -> {
            client.chat("测试");
        });
    }

    /**
     * 测试响应解析失败 - 格式错误
     */
    @Test
    void testInvalidResponseFormat() {
        // 模拟无效响应（缺少 choices）
        JSONObject responseJson = new JSONObject();
        responseJson.put("error", "invalid_request");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(
                responseJson.toJSONString(), HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 验证异常抛出
        assertThrows(RuntimeException.class, () -> {
            client.chat("测试");
        });
    }

    /**
     * 测试 isAvailable - 检查 API Key 是否有效
     */
    @Test
    void testIsAvailable() throws Exception {
        // 有 API Key 时应该可用
        assertTrue(client.isAvailable());

        // 使用反射将 apiKey 设为空字符串，测试 isAvailable() 返回 false
        Field apiKeyField = OpenAICompatibleClient.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(client, "");

        assertFalse(client.isAvailable());

        // 恢复 apiKey
        apiKeyField.set(client, TEST_API_KEY);
    }

    /**
     * 测试构造函数 - URL 自动补全 /v1
     */
    @Test
    void testConstructorUrlHandling() {
        // URL 已包含 /v1 时，不应重复添加
        OpenAICompatibleClient client1 = new OpenAICompatibleClient(
                "http://localhost:8000/v1", "key", "model", 2000, 0.7);
        assertNotNull(client1);

        // URL 不包含 /v1 时，应自动添加
        OpenAICompatibleClient client2 = new OpenAICompatibleClient(
                "http://localhost:8000", "key", "model", 2000, 0.7);
        assertNotNull(client2);
    }
}
