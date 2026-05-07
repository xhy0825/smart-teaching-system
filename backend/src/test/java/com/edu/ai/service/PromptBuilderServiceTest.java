package com.edu.ai.service;

import com.edu.ai.dto.QuestionGenerateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prompt构建服务测试
 */
class PromptBuilderServiceTest {

    private final PromptBuilderService promptBuilder = new PromptBuilderService();

    @Test
    void testBuildQuestionPrompt_MathChoice() {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setSubject("MATH");
        request.setQuestionType("CHOICE");
        request.setDifficulty(2);
        request.setCount(5);
        request.setKnowledgePoint("二次函数");

        String prompt = promptBuilder.buildQuestionPrompt(request);

        assertNotNull(prompt);
        assertTrue(prompt.contains("数学"));
        assertTrue(prompt.contains("选择题"));
        assertTrue(prompt.contains("中等"));
        assertTrue(prompt.contains("二次函数"));
        assertTrue(prompt.contains("JSON"));
        assertTrue(prompt.contains("5道"));
    }

    @Test
    void testBuildQuestionPrompt_PhysicsCalculation() {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setSubject("PHYSICS");
        request.setQuestionType("CALCULATION");
        request.setDifficulty(3);
        request.setCount(3);
        request.setKnowledgePoint("牛顿运动定律");

        String prompt = promptBuilder.buildQuestionPrompt(request);

        assertNotNull(prompt);
        assertTrue(prompt.contains("物理"));
        assertTrue(prompt.contains("计算题"));
        assertTrue(prompt.contains("困难"));
        assertTrue(prompt.contains("牛顿运动定律"));
        assertTrue(prompt.contains("3道"));
    }

    @Test
    void testBuildQuestionPrompt_EnglishFill() {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setSubject("ENGLISH");
        request.setQuestionType("FILL");
        request.setDifficulty(1);
        request.setCount(10);
        request.setAdditionalRequirements("单词填空，考察动词变形");

        String prompt = promptBuilder.buildQuestionPrompt(request);

        assertNotNull(prompt);
        assertTrue(prompt.contains("英语"));
        assertTrue(prompt.contains("填空题"));
        assertTrue(prompt.contains("简单"));
        assertTrue(prompt.contains("动词变形"));
        assertTrue(prompt.contains("10道"));
    }

    @Test
    void testBuildGradingPrompt() {
        String questionContent = "求解方程 x² - 4x + 3 = 0";
        String correctAnswer = "x₁ = 1, x₂ = 3";
        String studentAnswer = "x₁ = 1, x₂ = 3";
        double maxScore = 10.0;

        String prompt = promptBuilder.buildGradingPrompt(questionContent, correctAnswer, studentAnswer, maxScore);

        assertNotNull(prompt);
        assertTrue(prompt.contains("求解方程"));
        assertTrue(prompt.contains("x₁ = 1, x₂ = 3"));
        assertTrue(prompt.contains("10"));
        assertTrue(prompt.contains("score"));
        assertTrue(prompt.contains("isCorrect"));
        assertTrue(prompt.contains("analysis"));
    }

    @Test
    void testBuildGradingPrompt_PartialAnswer() {
        String questionContent = "计算物体的加速度";
        String correctAnswer = "a = F/m = 10/2 = 5 m/s²";
        String studentAnswer = "a = 5 m/s²";
        double maxScore = 8.0;

        String prompt = promptBuilder.buildGradingPrompt(questionContent, correctAnswer, studentAnswer, maxScore);

        assertNotNull(prompt);
        assertTrue(prompt.contains("加速度"));
        assertTrue(prompt.contains("8"));
        assertTrue(prompt.contains("评分标准"));
    }
}