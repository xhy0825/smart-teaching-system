package com.edu.user.service;

import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.AnswerMapper;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.service.ScoreAnalysisService;
import com.edu.grading.service.StudentWrongQuestionService;
import com.edu.user.dto.ClassProfileStatsResponse;
import com.edu.user.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassProfileServiceTest {

    @Mock
    private ScoreAnalysisService scoreAnalysisService;

    @Mock
    private StudentWrongQuestionService studentWrongQuestionService;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private com.edu.user.service.StudentService studentService;

    @Mock
    private com.edu.exam.mapper.QuestionMapper questionMapper;

    @Mock
    private com.edu.exam.mapper.ExamQuestionMapper examQuestionMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private AnswerSheetMapper answerSheetMapper;

    @Mock
    private com.edu.grading.mapper.StudentWrongQuestionMapper studentWrongQuestionMapper;

    @InjectMocks
    private ClassProfileService classProfileService;

    private Long testClassId;

    @BeforeEach
    void setUp() {
        testClassId = 1L;
    }

    @Test
    void testBuildKnowledgeMastery_EmptyClass() {
        when(studentService.listByClass(testClassId)).thenReturn(new ArrayList<>());

        List<ClassProfileStatsResponse.KnowledgeMastery> result =
                classProfileService.getClassStats(testClassId).getKnowledgeMastery();

        assertTrue(result.isEmpty(), "空班级应该返回空的掌握列表");
    }

    @Test
    void testBuildKnowledgeMastery_WithData() {
        // 模拟班级有1个学生
        Student s1 = new Student();
        s1.setId(1L);
        s1.setClassId(testClassId);
        List<Student> students = List.of(s1);
        when(studentService.listByClass(testClassId)).thenReturn(students);

        // 模拟答题卡（已批改）
        AnswerSheet sheet = new AnswerSheet();
        sheet.setId(10L);
        sheet.setExamPaperId(100L);
        sheet.setStudentId(1L);
        sheet.setStatus(3);
        when(answerSheetMapper.selectList(any())).thenReturn(List.of(sheet));

        // 模拟答案记录
        Answer answer = new Answer();
        answer.setId(100L);
        answer.setAnswerSheetId(10L);
        answer.setExamQuestionId(1000L);
        when(answerMapper.selectList(any())).thenReturn(List.of(answer));

        // 模拟 exam_question
        ExamQuestion eq = new ExamQuestion();
        eq.setId(1000L);
        eq.setExamPaperId(100L);
        eq.setQuestionId(100L);
        when(examQuestionMapper.selectList(any())).thenReturn(List.of(eq));

        // 模拟题目（有知识点）
        Question q = new Question();
        q.setId(100L);
        q.setKnowledgePoints("[\"代数\"]");
        Map<Long, Question> questionMap = new HashMap<>();
        questionMap.put(100L, q);
        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(q));

        // 模拟错题记录
        StudentWrongQuestion wq = new StudentWrongQuestion();
        wq.setStudentId(1L);
        wq.setQuestionId(100L);
        wq.setWrongCount(1);
        when(studentWrongQuestionMapper.selectList(any())).thenReturn(List.of(wq));

        // 模拟成绩分析（可以为null）
        when(scoreAnalysisService.getLatestByClassId(testClassId)).thenReturn(null);

        // 调用 getClassStats 会触发 buildKnowledgeMastery
        ClassProfileStatsResponse response = classProfileService.getClassStats(testClassId);

        assertNotNull(response.getKnowledgeMastery(), "知识点掌握列表不应该为空");
        assertFalse(response.getKnowledgeMastery().isEmpty(), "应该有知识点掌握数据");
        // 验证每个知识点的掌握率
        for (ClassProfileStatsResponse.KnowledgeMastery km : response.getKnowledgeMastery()) {
            assertTrue(km.getAvgMasteryRate().compareTo(new java.math.BigDecimal("0")) >= 0,
                    "掌握率应该 >= 0");
            assertTrue(km.getAvgMasteryRate().compareTo(new java.math.BigDecimal("100")) <= 0,
                    "掌握率应该 <= 100");
        }
    }

    @Test
    void testBuildKnowledgeRadar_WithData() {
        // 模拟班级有1个学生
        Student s1 = new Student();
        s1.setId(1L);
        s1.setClassId(testClassId);
        List<Student> students = List.of(s1);
        when(studentService.listByClass(testClassId)).thenReturn(students);

        // 模拟答题卡（已批改）
        AnswerSheet sheet = new AnswerSheet();
        sheet.setId(10L);
        sheet.setExamPaperId(100L);
        sheet.setStudentId(1L);
        sheet.setStatus(3);
        when(answerSheetMapper.selectList(any())).thenReturn(List.of(sheet));

        // 模拟答案记录
        Answer answer = new Answer();
        answer.setId(100L);
        answer.setAnswerSheetId(10L);
        answer.setExamQuestionId(1000L);
        when(answerMapper.selectList(any())).thenReturn(List.of(answer));

        // 模拟 exam_question
        ExamQuestion eq = new ExamQuestion();
        eq.setId(1000L);
        eq.setExamPaperId(100L);
        eq.setQuestionId(100L);
        when(examQuestionMapper.selectList(any())).thenReturn(List.of(eq));

        // 模拟题目（有知识点）
        Question q = new Question();
        q.setId(100L);
        q.setKnowledgePoints("[\"代数\"]");
        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(q));

        // 模拟错题记录
        StudentWrongQuestion wq = new StudentWrongQuestion();
        wq.setStudentId(1L);
        wq.setQuestionId(100L);
        wq.setWrongCount(1);
        when(studentWrongQuestionMapper.selectList(any())).thenReturn(List.of(wq));

        // 模拟成绩分析（可以为null）
        when(scoreAnalysisService.getLatestByClassId(testClassId)).thenReturn(null);

        // 调用 getClassStats
        ClassProfileStatsResponse response = classProfileService.getClassStats(testClassId);

        // 验证雷达图数据
        assertNotNull(response.getKnowledgeRadar(), "雷达图数据不应该为空");
        assertFalse(response.getKnowledgeRadar().getPoints().isEmpty(), "雷达图知识点列表不应该为空");
        assertFalse(response.getKnowledgeRadar().getScores().isEmpty(), "雷达图分数列表不应该为空");
        assertEquals(response.getKnowledgeRadar().getPoints().size(), response.getKnowledgeRadar().getScores().size(), "知识点和分数数量应该一致");
        // 验证分数范围
        for (java.math.BigDecimal score : response.getKnowledgeRadar().getScores()) {
            assertTrue(score.compareTo(new java.math.BigDecimal("0")) >= 0, "掌握率应该 >= 0");
            assertTrue(score.compareTo(new java.math.BigDecimal("100")) <= 0, "掌握率应该 <= 100");
        }
    }

    @Test
    void testBuildScoreBoxplot_WithData() {
        // 模拟班级有1个学生
        Student s1 = new Student();
        s1.setId(1L);
        s1.setClassId(testClassId);
        List<Student> students = List.of(s1);
        when(studentService.listByClass(testClassId)).thenReturn(students);

        // 模拟答题卡（已批改，有成绩）
        AnswerSheet sheet1 = new AnswerSheet();
        sheet1.setId(10L);
        sheet1.setStudentId(1L);
        sheet1.setStatus(3);
        sheet1.setTotalScore(new java.math.BigDecimal("85.5"));
        AnswerSheet sheet2 = new AnswerSheet();
        sheet2.setId(11L);
        sheet2.setStudentId(1L);
        sheet2.setStatus(3);
        sheet2.setTotalScore(new java.math.BigDecimal("92.0"));
        when(answerSheetMapper.selectList(any())).thenReturn(List.of(sheet1, sheet2));

        // 模拟成绩分析（可以为null）
        when(scoreAnalysisService.getLatestByClassId(testClassId)).thenReturn(null);

        // 调用 getClassStats
        ClassProfileStatsResponse response = classProfileService.getClassStats(testClassId);

        // 验证箱线图数据
        assertNotNull(response.getScoreBoxplot(), "箱线图数据不应该为空");
        assertNotNull(response.getScoreBoxplot().getMin(), "最小值不应该为空");
        assertNotNull(response.getScoreBoxplot().getMax(), "最大值不应该为空");
        assertNotNull(response.getScoreBoxplot().getMedian(), "中位数不应该为空");
        // 验证五数概括的合理性
        assertTrue(response.getScoreBoxplot().getMin().compareTo(response.getScoreBoxplot().getMax()) <= 0, "最小值应该 <= 最大值");
        assertTrue(response.getScoreBoxplot().getQ1().compareTo(response.getScoreBoxplot().getQ3()) <= 0, "Q1 应该 <= Q3");
    }
}
