package com.edu.ai.service;

import com.edu.ai.dto.QuestionGenerateRequest;
import com.edu.ai.dto.QuestionGenerateResponse;
import com.edu.ai.dto.GradingRequest;
import com.edu.ai.dto.GradingResponse;
import com.edu.ai.provider.AIProvider;
import com.edu.ai.provider.AIProviderFactory;
import com.edu.common.util.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * AI服务测试
 */
@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private AIProviderFactory providerFactory;

    @Mock
    private AIProvider aiProvider;

    @InjectMocks
    private AIService aiService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testGenerateQuestions_Success() {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setSubject("MATH");
        request.setQuestionType("CHOICE");
        request.setDifficulty(2);
        request.setCount(5);

        QuestionGenerateResponse mockResponse = new QuestionGenerateResponse();
        mockResponse.setSuccess(true);

        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.getName()).thenReturn("Cloud-Claude");
        when(aiProvider.generateQuestions(any(QuestionGenerateRequest.class))).thenReturn(mockResponse);

        QuestionGenerateResponse result = aiService.generateQuestions(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(aiProvider, times(1)).generateQuestions(any(QuestionGenerateRequest.class));
    }

    @Test
    void testGradeSubjectiveQuestion_Success() {
        GradingRequest request = new GradingRequest();
        request.setQuestionContent("测试题目");
        request.setCorrectAnswer("正确答案");
        request.setStudentAnswer("学生答案");
        request.setMaxScore(10.0);

        GradingResponse mockResponse = new GradingResponse();
        mockResponse.setSuccess(true);

        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.getName()).thenReturn("Cloud-Claude");
        when(aiProvider.gradeSubjectiveQuestion(any(GradingRequest.class))).thenReturn(mockResponse);

        GradingResponse result = aiService.gradeSubjectiveQuestion(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(aiProvider, times(1)).gradeSubjectiveQuestion(any(GradingRequest.class));
    }

    @Test
    void testCheckStatus_Available() {
        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.isAvailable()).thenReturn(true);

        boolean status = aiService.checkStatus();

        assertTrue(status);
    }

    @Test
    void testCheckStatus_NotAvailable() {
        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.isAvailable()).thenReturn(false);

        boolean status = aiService.checkStatus();

        assertFalse(status);
    }

    @Test
    void testGetCurrentProviderName() {
        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.getName()).thenReturn("Cloud-Claude");

        String name = aiService.getCurrentProviderName();

        assertEquals("Cloud-Claude", name);
    }

    @Test
    void testGetUsageStats() {
        when(providerFactory.getProvider(anyLong())).thenReturn(aiProvider);
        when(aiProvider.getName()).thenReturn("Cloud-Claude");
        when(aiProvider.getCallCount()).thenReturn(100L);
        when(aiProvider.getTokenCount()).thenReturn(5000L);
        when(aiProvider.isAvailable()).thenReturn(true);

        AIService.AIUsageStats stats = aiService.getUsageStats();

        assertNotNull(stats);
        assertEquals("Cloud-Claude", stats.getProviderName());
        assertEquals(100L, stats.getCallCount());
        assertEquals(5000L, stats.getTokenCount());
        assertTrue(stats.isAvailable());
    }

    @Test
    void testGenerateQuestions_TenantContextMissing() {
        TenantContextHolder.clear();

        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setSubject("MATH");
        request.setCount(5);

        when(providerFactory.getDefaultProvider()).thenReturn(aiProvider);
        when(aiProvider.getName()).thenReturn("Default-Cloud");

        aiService.generateQuestions(request);

        verify(providerFactory, times(1)).getDefaultProvider();
    }
}