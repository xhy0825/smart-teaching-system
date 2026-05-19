package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.entity.StudentWrongQuestion;
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

import java.math.BigDecimal;
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

    @Test
    void testGetKnowledgePointStats_WithWrongQuestions() {
        // 模拟错题记录
        StudentWrongQuestion wq1 = new StudentWrongQuestion();
        wq1.setStudentId(testStudentId);
        wq1.setQuestionId(100L);

        StudentWrongQuestion wq2 = new StudentWrongQuestion();
        wq2.setStudentId(testStudentId);
        wq2.setQuestionId(101L);

        List<StudentWrongQuestion> wrongQuestions = List.of(wq1, wq2);
        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(wrongQuestions);

        // 模拟题目信息 - 题目100有知识点["代数", "函数"]
        Question q1 = new Question();
        q1.setId(100L);
        q1.setKnowledgePoints("[\"代数\", \"函数\"]");

        // 题目101有知识点["几何"]
        Question q2 = new Question();
        q2.setId(101L);
        q2.setKnowledgePoints("[\"几何\"]");

        when(questionService.getById(100L)).thenReturn(q1);
        when(questionService.getById(101L)).thenReturn(q2);

        List<StudentProfileResponse.KnowledgePointStats> stats =
                studentProfileService.getKnowledgePointStats(testStudentId);

        assertFalse(stats.isEmpty(), "应该有知识点统计");
        // 总共3个知识点：代数、函数、几何
        assertEquals(3, stats.size(), "应该有3个知识点统计");

        // 验证每个知识点的掌握率计算
        for (StudentProfileResponse.KnowledgePointStats stat : stats) {
            assertTrue(stat.getMasteryRate().compareTo(new BigDecimal("0")) >= 0,
                    "掌握率应该 >= 0");
            assertTrue(stat.getMasteryRate().compareTo(new BigDecimal("100")) <= 0,
                    "掌握率应该 <= 100");
        }
    }

    @Test
    void testBuildWrongTypePie_NoWrongQuestions() {
        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        StudentProfileResponse.WrongTypePie pie =
                studentProfileService.buildWrongTypePie(testStudentId);

        assertTrue(pie.getTypes().isEmpty(), "没有错题时 types 应该为空");
        assertTrue(pie.getCounts().isEmpty(), "没有错题时 counts 应该为空");
    }

    @Test
    void testBuildWrongTypePie_WithWrongQuestions() {
        // 模拟3道错题：2道选择题，1道填空题
        StudentWrongQuestion wq1 = new StudentWrongQuestion();
        wq1.setQuestionId(100L);

        StudentWrongQuestion wq2 = new StudentWrongQuestion();
        wq2.setQuestionId(101L);

        StudentWrongQuestion wq3 = new StudentWrongQuestion();
        wq3.setQuestionId(102L);

        List<StudentWrongQuestion> wrongQuestions = List.of(wq1, wq2, wq3);
        when(wrongQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(wrongQuestions);

        // 模拟题目信息
        Question q1 = new Question();
        q1.setId(100L);
        q1.setQuestionType("CHOICE");

        Question q2 = new Question();
        q2.setId(101L);
        q2.setQuestionType("CHOICE");

        Question q3 = new Question();
        q3.setId(102L);
        q3.setQuestionType("FILL");

        when(questionService.getById(100L)).thenReturn(q1);
        when(questionService.getById(101L)).thenReturn(q2);
        when(questionService.getById(102L)).thenReturn(q3);

        StudentProfileResponse.WrongTypePie pie =
                studentProfileService.buildWrongTypePie(testStudentId);

        assertEquals(2, pie.getTypes().size(), "应该有2种题型");
        assertEquals(2, pie.getCounts().get(0).intValue(), "选择题应该有2道");
        assertEquals(1, pie.getCounts().get(1).intValue(), "填空题应该有1道");
    }
}
