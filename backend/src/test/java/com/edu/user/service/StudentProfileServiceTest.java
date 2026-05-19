package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
import com.edu.user.dto.StudentProfileResponse;
import com.edu.user.entity.Student;
import com.edu.user.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private AnswerSheetMapper answerSheetMapper;

    @Mock
    private StudentWrongQuestionMapper wrongQuestionMapper;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private Long testStudentId;

    @BeforeEach
    void setUp() {
        testStudentId = 1L;
    }

    @Test
    void testGetKnowledgePointStats_NoWrongQuestions() {
        // 模拟没有错题记录
        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        List<StudentProfileResponse.KnowledgePointStats> stats =
                studentProfileService.getKnowledgePointStats(testStudentId);

        assertTrue(stats.isEmpty(), "没有错题时应该返回空列表");
    }
}
