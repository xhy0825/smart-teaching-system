package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import com.edu.exam.service.ExamPaperService;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 批改服务测试
 */
@ExtendWith(MockitoExtension.class)
class GradingServiceTest {

    @Mock
    private AnswerSheetService answerSheetService;

    @Mock
    private AnswerService answerService;

    @Mock
    private ExamPaperService examPaperService;

    @Mock
    private QuestionService questionService;

    @Mock
    private StudentWrongQuestionService wrongQuestionService;

    @InjectMocks
    private GradingService gradingService;

    private AnswerSheet mockAnswerSheet;
    private Question mockQuestion;
    private ExamQuestion mockExamQuestion;
    private Answer mockAnswer;

    @BeforeEach
    void setUp() {
        mockAnswerSheet = new AnswerSheet();
        mockAnswerSheet.setId(1L);
        mockAnswerSheet.setExamPaperId(100L);
        mockAnswerSheet.setStudentId(1L);
        mockAnswerSheet.setStatus(2);

        mockQuestion = new Question();
        mockQuestion.setId(1L);
        mockQuestion.setQuestionType("CHOICE");
        mockQuestion.setAnswer("A");

        mockExamQuestion = new ExamQuestion();
        mockExamQuestion.setId(1L);
        mockExamQuestion.setQuestionId(1L);
        mockExamQuestion.setScore(BigDecimal.valueOf(4));

        mockAnswer = new Answer();
        mockAnswer.setId(1L);
        mockAnswer.setStudentAnswer("A");
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testGradeAnswerSheet_Success() {
        List<ExamQuestion> examQuestions = Arrays.asList(mockExamQuestion);

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        doNothing().when(answerSheetService).startGrading(anyLong());
        when(examPaperService.getPaperQuestions(100L)).thenReturn(examQuestions);
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(wrongQuestionService).recordWrongQuestion(anyLong(), anyLong(), anyLong());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        verify(answerSheetService, times(1)).startGrading(1L);
        verify(answerSheetService, times(1)).completeGrading(anyLong(), any(BigDecimal.class), anyLong());
    }

    @Test
    void testGradeAnswerSheet_NotFound() {
        when(answerSheetService.getById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> gradingService.gradeAnswerSheet(1L, 1L));
    }

    @Test
    void testGradeChoice_CorrectAnswer() {
        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        // 选择题正确答案应得满分
        verify(answerService).updateScore(anyLong(), eq(BigDecimal.valueOf(4)), eq(1));
    }

    @Test
    void testGradeChoice_WrongAnswer() {
        mockAnswer.setStudentAnswer("B");

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(wrongQuestionService).recordWrongQuestion(anyLong(), anyLong(), anyLong());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        verify(answerService).updateScore(anyLong(), eq(BigDecimal.ZERO), eq(0));
        verify(wrongQuestionService).recordWrongQuestion(anyLong(), anyLong(), anyLong());
    }

    @Test
    void testGradeCalculation_CorrectAnswer() {
        mockQuestion.setQuestionType("CALCULATION");
        mockQuestion.setAnswer("100");
        mockAnswer.setStudentAnswer("100");

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        verify(answerService).updateScore(anyLong(), eq(BigDecimal.valueOf(4)), eq(1));
    }

    @Test
    void testGradeCalculation_ApproximateAnswer() {
        mockQuestion.setQuestionType("CALCULATION");
        mockQuestion.setAnswer("100");
        mockAnswer.setStudentAnswer("100.005");

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        // 允许0.01误差，100.005 - 100 = 0.005 <= 0.01
        verify(answerService).updateScore(anyLong(), eq(BigDecimal.valueOf(4)), eq(1));
    }

    @Test
    void testGradeFill_MultipleAnswers() {
        mockQuestion.setQuestionType("FILL");
        mockQuestion.setAnswer("答案一|答案二");
        mockAnswer.setStudentAnswer("答案一");

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        verify(answerService).updateScore(anyLong(), eq(BigDecimal.valueOf(4)), eq(1));
    }

    @Test
    void testGradeJudge_CorrectAnswer() {
        mockQuestion.setQuestionType("JUDGE");
        mockQuestion.setAnswer("正确");
        mockAnswer.setStudentAnswer("对");

        when(answerSheetService.getById(1L)).thenReturn(mockAnswerSheet);
        when(examPaperService.getPaperQuestions(100L)).thenReturn(Arrays.asList(mockExamQuestion));
        when(answerService.getBySheetAndQuestion(anyLong(), anyLong())).thenReturn(mockAnswer);
        when(questionService.getQuestionById(1L)).thenReturn(mockQuestion);
        doNothing().when(answerService).updateScore(anyLong(), any(BigDecimal.class), anyInt());
        doNothing().when(answerSheetService).completeGrading(anyLong(), any(BigDecimal.class), anyLong());

        gradingService.gradeAnswerSheet(1L, 1L);

        verify(answerService).updateScore(anyLong(), eq(BigDecimal.valueOf(4)), eq(1));
    }
}