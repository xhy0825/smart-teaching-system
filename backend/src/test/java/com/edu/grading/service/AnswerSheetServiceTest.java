package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.mapper.AnswerSheetMapper;
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
 * 答题卡服务测试
 */
@ExtendWith(MockitoExtension.class)
class AnswerSheetServiceTest {

    @Mock
    private AnswerSheetMapper answerSheetMapper;

    @InjectMocks
    private AnswerSheetService answerSheetService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreateAnswerSheet_Success() {
        when(answerSheetMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(answerSheetMapper.insert(any(AnswerSheet.class))).thenReturn(1);

        AnswerSheet result = answerSheetService.createAnswerSheet(100L, 1L);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        assertEquals(1L, result.getTenantId());
    }

    @Test
    void testCreateAnswerSheet_TenantContextMissing() {
        TenantContextHolder.clear();

        assertThrows(BusinessException.class, () -> answerSheetService.createAnswerSheet(100L, 1L));
    }

    @Test
    void testCreateAnswerSheet_AlreadyExists() {
        when(answerSheetMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> answerSheetService.createAnswerSheet(100L, 1L));
    }

    @Test
    void testSubmitAnswerSheet_Success() {
        AnswerSheet sheet = new AnswerSheet();
        sheet.setId(1L);
        sheet.setStatus(0);

        when(answerSheetMapper.selectById(1L)).thenReturn(sheet);
        when(answerSheetMapper.updateById(any(AnswerSheet.class))).thenReturn(1);

        answerSheetService.submitAnswerSheet(1L);

        assertEquals(1, sheet.getStatus());
        assertNotNull(sheet.getSubmitTime());
    }

    @Test
    void testSubmitAnswerSheet_NotFound() {
        when(answerSheetMapper.selectById(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> answerSheetService.submitAnswerSheet(1L));
    }

    @Test
    void testCompleteGrading_Success() {
        AnswerSheet sheet = new AnswerSheet();
        sheet.setId(1L);
        sheet.setStatus(2);

        when(answerSheetMapper.selectById(1L)).thenReturn(sheet);
        when(answerSheetMapper.updateById(any(AnswerSheet.class))).thenReturn(1);

        answerSheetService.completeGrading(1L, BigDecimal.valueOf(85), 1L);

        assertEquals(3, sheet.getStatus());
        assertEquals(BigDecimal.valueOf(85), sheet.getTotalScore());
        assertNotNull(sheet.getGradingTime());
        assertEquals(1L, sheet.getGradedBy());
    }

    @Test
    void testListByExam_Success() {
        AnswerSheet sheet1 = new AnswerSheet();
        sheet1.setId(1L);
        sheet1.setExamPaperId(100L);

        AnswerSheet sheet2 = new AnswerSheet();
        sheet2.setId(2L);
        sheet2.setExamPaperId(100L);

        when(answerSheetMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sheet1, sheet2));

        List<AnswerSheet> result = answerSheetService.listByExam(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testCountByExam_Success() {
        when(answerSheetMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(30L);

        int count = answerSheetService.countByExam(100L);

        assertEquals(30, count);
    }

    @Test
    void testCountGradedByExam_Success() {
        when(answerSheetMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(25L);

        int count = answerSheetService.countGradedByExam(100L);

        assertEquals(25, count);
    }
}