package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.mapper.ExamPaperMapper;
import com.edu.exam.mapper.ExamQuestionMapper;
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
 * 试卷服务测试
 */
@ExtendWith(MockitoExtension.class)
class ExamPaperServiceTest {

    @Mock
    private ExamPaperMapper examPaperMapper;

    @Mock
    private ExamQuestionMapper examQuestionMapper;

    @InjectMocks
    private ExamPaperService examPaperService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreatePaper_Success() {
        ExamPaper paper = new ExamPaper();
        paper.setTitle("测试试卷");
        paper.setSubject("MATH");
        paper.setTotalScore(BigDecimal.valueOf(100));
        paper.setCreatedBy(1L);

        when(examPaperMapper.insert(any(ExamPaper.class))).thenReturn(1);
        when(examPaperMapper.selectById(anyLong())).thenReturn(paper);

        ExamPaper result = examPaperService.createPaper(paper);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        assertEquals(1L, result.getTenantId());
        verify(examPaperMapper, times(1)).insert(any(ExamPaper.class));
    }

    @Test
    void testCreatePaper_TenantContextMissing() {
        TenantContextHolder.clear();
        ExamPaper paper = new ExamPaper();
        paper.setTitle("测试试卷");

        assertThrows(BusinessException.class, () -> examPaperService.createPaper(paper));
    }

    @Test
    void testPublishPaper_Success() {
        ExamPaper paper = new ExamPaper();
        paper.setId(1L);
        paper.setStatus(0);

        when(examPaperMapper.selectById(1L)).thenReturn(paper);
        when(examPaperMapper.updateById(any(ExamPaper.class))).thenReturn(1);

        examPaperService.publishPaper(1L);

        assertEquals(1, paper.getStatus());
        assertNotNull(paper.getPublishedAt());
        verify(examPaperMapper, times(1)).updateById(any(ExamPaper.class));
    }

    @Test
    void testPublishPaper_NotFound() {
        when(examPaperMapper.selectById(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> examPaperService.publishPaper(1L));
    }

    @Test
    void testDeletePaper_Success() {
        when(examPaperMapper.deleteById(1L)).thenReturn(1);
        when(examQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);

        examPaperService.deletePaper(1L);

        verify(examPaperMapper, times(1)).deleteById(1L);
        verify(examQuestionMapper, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void testAddQuestionToPaper_Success() {
        when(examQuestionMapper.insert(any(ExamQuestion.class))).thenReturn(1);

        examPaperService.addQuestionToPaper(1L, 100L, 1, BigDecimal.valueOf(4));

        verify(examQuestionMapper, times(1)).insert(any(ExamQuestion.class));
    }

    @Test
    void testGetPaperQuestions_Success() {
        ExamQuestion eq1 = new ExamQuestion();
        eq1.setExamPaperId(1L);
        eq1.setQuestionId(100L);
        eq1.setSequence(1);
        eq1.setScore(BigDecimal.valueOf(4));

        ExamQuestion eq2 = new ExamQuestion();
        eq2.setExamPaperId(1L);
        eq2.setQuestionId(101L);
        eq2.setSequence(2);
        eq2.setScore(BigDecimal.valueOf(6));

        when(examQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(eq1, eq2));

        List<ExamQuestion> result = examPaperService.getPaperQuestions(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getSequence());
        assertEquals(2, result.get(1).getSequence());
    }

    @Test
    void testListByTenant_Success() {
        ExamPaper paper1 = new ExamPaper();
        paper1.setId(1L);
        paper1.setTitle("试卷1");

        ExamPaper paper2 = new ExamPaper();
        paper2.setId(2L);
        paper2.setTitle("试卷2");

        when(examPaperMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(paper1, paper2));

        List<ExamPaper> result = examPaperService.listByTenant();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}