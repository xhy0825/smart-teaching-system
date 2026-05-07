package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
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
 * 错题记录服务测试
 */
@ExtendWith(MockitoExtension.class)
class StudentWrongQuestionServiceTest {

    @Mock
    private StudentWrongQuestionMapper wrongQuestionMapper;

    @InjectMocks
    private StudentWrongQuestionService wrongQuestionService;

    @Test
    void testRecordWrongQuestion_NewRecord() {
        when(wrongQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(wrongQuestionMapper.insert(any(StudentWrongQuestion.class))).thenReturn(1);

        wrongQuestionService.recordWrongQuestion(1L, 100L, 1000L);

        verify(wrongQuestionMapper, times(1)).insert(any(StudentWrongQuestion.class));
    }

    @Test
    void testRecordWrongQuestion_UpdateExisting() {
        StudentWrongQuestion existing = new StudentWrongQuestion();
        existing.setId(1L);
        existing.setStudentId(1L);
        existing.setQuestionId(100L);
        existing.setWrongCount(1);

        when(wrongQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(wrongQuestionMapper.updateById(any(StudentWrongQuestion.class))).thenReturn(1);

        wrongQuestionService.recordWrongQuestion(1L, 100L, 1000L);

        assertEquals(2, existing.getWrongCount());
        assertNull(existing.getCorrectedAt());  // 重置纠错标记
        verify(wrongQuestionMapper, times(1)).updateById(any(StudentWrongQuestion.class));
    }

    @Test
    void testMarkCorrected_Success() {
        StudentWrongQuestion existing = new StudentWrongQuestion();
        existing.setId(1L);
        existing.setStudentId(1L);
        existing.setQuestionId(100L);

        when(wrongQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(wrongQuestionMapper.updateById(any(StudentWrongQuestion.class))).thenReturn(1);

        wrongQuestionService.markCorrected(1L, 100L);

        assertNotNull(existing.getCorrectedAt());
        verify(wrongQuestionMapper, times(1)).updateById(any(StudentWrongQuestion.class));
    }

    @Test
    void testMarkCorrected_NotFound() {
        when(wrongQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        wrongQuestionService.markCorrected(1L, 100L);

        verify(wrongQuestionMapper, never()).updateById(any(StudentWrongQuestion.class));
    }

    @Test
    void testListByStudent_Success() {
        StudentWrongQuestion q1 = new StudentWrongQuestion();
        q1.setId(1L);
        q1.setStudentId(1L);
        q1.setWrongCount(2);

        StudentWrongQuestion q2 = new StudentWrongQuestion();
        q2.setId(2L);
        q2.setStudentId(1L);
        q2.setWrongCount(3);

        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(q1, q2));

        List<StudentWrongQuestion> result = wrongQuestionService.listByStudent(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListFrequentWrong_Success() {
        StudentWrongQuestion q1 = new StudentWrongQuestion();
        q1.setId(1L);
        q1.setWrongCount(5);

        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(q1));

        List<StudentWrongQuestion> result = wrongQuestionService.listFrequentWrong(20L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCountByStudent_Success() {
        when(wrongQuestionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        int count = wrongQuestionService.countByStudent(1L);

        assertEquals(10, count);
    }
}