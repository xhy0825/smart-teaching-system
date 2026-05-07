package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.Question;
import com.edu.exam.mapper.QuestionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 题目服务测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private Question mockQuestion;

    @BeforeEach
    void setUp() {
        mockQuestion = new Question();
        mockQuestion.setId(1L);
        mockQuestion.setBankId(100L);
        mockQuestion.setSubject("MATH");
        mockQuestion.setQuestionType("CHOICE");
        mockQuestion.setDifficulty(2);
        mockQuestion.setContent("测试题目");
        mockQuestion.setAnswer("A");
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreateQuestion_Success() {
        when(questionMapper.insert(any(Question.class))).thenReturn(1);
        when(questionMapper.selectById(anyLong())).thenReturn(mockQuestion);

        Question result = questionService.createQuestion(mockQuestion);

        assertNotNull(result);
        assertEquals("MANUAL", result.getSource());
        verify(questionMapper, times(1)).insert(any(Question.class));
    }

    @Test
    void testListByBank_Success() {
        Question q1 = new Question();
        q1.setId(1L);
        q1.setBankId(100L);

        Question q2 = new Question();
        q2.setId(2L);
        q2.setBankId(100L);

        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(q1, q2));

        List<Question> result = questionService.listByBank(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testQueryQuestions_BySubjectAndType() {
        Question q1 = new Question();
        q1.setId(1L);
        q1.setSubject("MATH");
        q1.setQuestionType("CHOICE");

        Question q2 = new Question();
        q2.setId(2L);
        q2.setSubject("MATH");
        q2.setQuestionType("CHOICE");

        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(q1, q2));

        List<Question> result = questionService.queryQuestions("MATH", "CHOICE", null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testQueryQuestions_ByDifficulty() {
        Question q1 = new Question();
        q1.setId(1L);
        q1.setSubject("MATH");
        q1.setDifficulty(2);

        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(q1));

        List<Question> result = questionService.queryQuestions("MATH", null, 2, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getDifficulty());
    }

    @Test
    void testGetQuestionById_Success() {
        when(questionMapper.selectById(1L)).thenReturn(mockQuestion);

        Question result = questionService.getQuestionById(1L);

        assertNotNull(result);
        assertEquals("MATH", result.getSubject());
    }

    @Test
    void testUpdateQuestion_Success() {
        when(questionMapper.updateById(any(Question.class))).thenReturn(1);

        mockQuestion.setContent("更新后的内容");
        questionService.updateQuestion(mockQuestion);

        verify(questionMapper, times(1)).updateById(any(Question.class));
    }

    @Test
    void testDeleteQuestion_Success() {
        when(questionMapper.deleteById(1L)).thenReturn(1);

        questionService.deleteQuestion(1L);

        verify(questionMapper, times(1)).deleteById(1L);
    }

    @Test
    void testCountByBank_Success() {
        when(questionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        int count = questionService.countByBank(100L);

        assertEquals(10, count);
    }
}