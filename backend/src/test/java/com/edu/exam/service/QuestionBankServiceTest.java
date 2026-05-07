package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.QuestionBank;
import com.edu.exam.mapper.QuestionBankMapper;
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
 * 题库服务测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionBankServiceTest {

    @Mock
    private QuestionBankMapper questionBankMapper;

    @InjectMocks
    private QuestionBankService questionBankService;

    private QuestionBank mockBank;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
        mockBank = new QuestionBank();
        mockBank.setId(1L);
        mockBank.setName("数学题库");
        mockBank.setSubject("MATH");
        mockBank.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreateBank_Success() {
        when(questionBankMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(questionBankMapper.insert(any(QuestionBank.class))).thenReturn(1);
        when(questionBankMapper.selectById(anyLong())).thenReturn(mockBank);

        QuestionBank result = questionBankService.createBank(mockBank);

        assertNotNull(result);
        assertEquals(0, result.getIsPublic());
        assertEquals(1L, result.getTenantId());
        verify(questionBankMapper, times(1)).insert(any(QuestionBank.class));
    }

    @Test
    void testCreateBank_TenantContextMissing() {
        TenantContextHolder.clear();

        assertThrows(BusinessException.class, () -> questionBankService.createBank(mockBank));
    }

    @Test
    void testListByTenant_Success() {
        QuestionBank bank1 = new QuestionBank();
        bank1.setId(1L);
        bank1.setName("题库1");

        QuestionBank bank2 = new QuestionBank();
        bank2.setId(2L);
        bank2.setName("题库2");

        when(questionBankMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(bank1, bank2));

        List<QuestionBank> result = questionBankService.listByTenant();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListBySubject_Success() {
        QuestionBank bank1 = new QuestionBank();
        bank1.setId(1L);
        bank1.setSubject("MATH");

        when(questionBankMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(bank1));

        List<QuestionBank> result = questionBankService.listBySubject("MATH");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MATH", result.get(0).getSubject());
    }

    @Test
    void testGetBankById_Success() {
        when(questionBankMapper.selectById(1L)).thenReturn(mockBank);

        QuestionBank result = questionBankService.getBankById(1L);

        assertNotNull(result);
        assertEquals("数学题库", result.getName());
    }

    @Test
    void testUpdateBank_Success() {
        when(questionBankMapper.updateById(any(QuestionBank.class))).thenReturn(1);

        mockBank.setName("更新后的题库");
        questionBankService.updateBank(mockBank);

        verify(questionBankMapper, times(1)).updateById(any(QuestionBank.class));
    }

    @Test
    void testDeleteBank_Success() {
        when(questionBankMapper.deleteById(1L)).thenReturn(1);

        questionBankService.deleteBank(1L);

        verify(questionBankMapper, times(1)).deleteById(1L);
    }
}