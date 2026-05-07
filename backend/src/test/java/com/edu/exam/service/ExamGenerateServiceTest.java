package com.edu.exam.service;

import com.edu.exam.dto.ExamGenerateRequest;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 试卷生成服务测试
 */
@ExtendWith(MockitoExtension.class)
class ExamGenerateServiceTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private ExamPaperService examPaperService;

    @Mock
    private QuestionBankService questionBankService;

    @InjectMocks
    private ExamGenerateService examGenerateService;

    private ExamGenerateRequest request;
    private List<Question> mockQuestions;

    @BeforeEach
    void setUp() {
        request = new ExamGenerateRequest();
        request.setTitle("数学单元测试");
        request.setSubject("MATH");
        request.setGradeId(1L);
        request.setClassId(1L);
        request.setTotalScore(100.0);
        request.setTimeLimit(45);
        request.setStructure("[{\"section\":\"选择题\",\"type\":\"CHOICE\",\"count\":5,\"scoreEach\":4}]");
        request.setCreatedBy(1L);

        mockQuestions = Arrays.asList(
                createQuestion(1L, "CHOICE", 1),
                createQuestion(2L, "CHOICE", 2),
                createQuestion(3L, "CHOICE", 1),
                createQuestion(4L, "CHOICE", 2),
                createQuestion(5L, "CHOICE", 1)
        );
    }

    @Test
    void testGenerateExam_Success() {
        ExamPaper mockPaper = new ExamPaper();
        mockPaper.setId(100L);
        mockPaper.setTitle("数学单元测试");

        when(examPaperService.createPaper(any(ExamPaper.class))).thenReturn(mockPaper);
        when(questionService.queryQuestions(anyString(), anyString(), any(), any()))
                .thenReturn(mockQuestions);
        doNothing().when(examPaperService).addQuestionToPaper(anyLong(), anyLong(), anyInt(), any(BigDecimal.class));

        ExamPaper result = examGenerateService.generateExam(request);

        assertNotNull(result);
        assertEquals("数学单元测试", result.getTitle());
        verify(examPaperService, times(1)).createPaper(any(ExamPaper.class));
        verify(questionService, times(1)).queryQuestions(eq("MATH"), eq("CHOICE"), any(), any());
        verify(examPaperService, times(5)).addQuestionToPaper(eq(100L), anyLong(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testGenerateExam_WithMultipleSections() {
        request.setStructure("[{\"section\":\"选择题\",\"type\":\"CHOICE\",\"count\":3,\"scoreEach\":4}," +
                "{\"section\":\"填空题\",\"type\":\"FILL\",\"count\":2,\"scoreEach\":6}]");

        ExamPaper mockPaper = new ExamPaper();
        mockPaper.setId(100L);

        List<Question> choiceQuestions = Arrays.asList(
                createQuestion(1L, "CHOICE", 1),
                createQuestion(2L, "CHOICE", 2),
                createQuestion(3L, "CHOICE", 1)
        );
        List<Question> fillQuestions = Arrays.asList(
                createQuestion(4L, "FILL", 2),
                createQuestion(5L, "FILL", 1)
        );

        when(examPaperService.createPaper(any(ExamPaper.class))).thenReturn(mockPaper);
        when(questionService.queryQuestions(eq("MATH"), eq("CHOICE"), any(), any()))
                .thenReturn(choiceQuestions);
        when(questionService.queryQuestions(eq("MATH"), eq("FILL"), any(), any()))
                .thenReturn(fillQuestions);
        doNothing().when(examPaperService).addQuestionToPaper(anyLong(), anyLong(), anyInt(), any(BigDecimal.class));

        ExamPaper result = examGenerateService.generateExam(request);

        assertNotNull(result);
        verify(questionService, times(2)).queryQuestions(anyString(), anyString(), any(), any());
        verify(examPaperService, times(5)).addQuestionToPaper(eq(100L), anyLong(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testGenerateExam_QuestionCountInsufficient() {
        request.setStructure("[{\"section\":\"选择题\",\"type\":\"CHOICE\",\"count\":10,\"scoreEach\":4}]");

        ExamPaper mockPaper = new ExamPaper();
        mockPaper.setId(100L);

        List<Question> insufficientQuestions = Arrays.asList(
                createQuestion(1L, "CHOICE", 1),
                createQuestion(2L, "CHOICE", 2)
        );

        when(examPaperService.createPaper(any(ExamPaper.class))).thenReturn(mockPaper);
        when(questionService.queryQuestions(anyString(), anyString(), any(), any()))
                .thenReturn(insufficientQuestions);
        doNothing().when(examPaperService).addQuestionToPaper(anyLong(), anyLong(), anyInt(), any(BigDecimal.class));

        ExamPaper result = examGenerateService.generateExam(request);

        assertNotNull(result);
        verify(examPaperService, times(2)).addQuestionToPaper(eq(100L), anyLong(), anyInt(), any(BigDecimal.class));
    }

    private Question createQuestion(Long id, String type, Integer difficulty) {
        Question q = new Question();
        q.setId(id);
        q.setSubject("MATH");
        q.setQuestionType(type);
        q.setDifficulty(difficulty);
        q.setContent("测试题目内容 " + id);
        q.setAnswer("答案 " + id);
        return q;
    }
}