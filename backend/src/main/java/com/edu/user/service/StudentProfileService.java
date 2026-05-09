package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.AnswerSheetMapper;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
import com.edu.user.dto.StudentProfileResponse;
import com.edu.user.entity.Student;
import com.edu.user.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生画像服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentMapper studentMapper;
    private final AnswerSheetMapper answerSheetMapper;
    private final StudentWrongQuestionMapper wrongQuestionMapper;

    /**
     * 获取学生画像
     */
    public StudentProfileResponse getStudentProfile(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return null;
        }

        StudentProfileResponse response = new StudentProfileResponse();

        // 基本信息
        response.setId(student.getId());
        response.setName(student.getName());
        response.setStudentNo(student.getStudentNo());
        response.setClassId(student.getClassId());
        response.setGender(student.getGender());
        response.setBirthDate(student.getBirthDate());

        // 计算年龄
        if (student.getBirthDate() != null) {
            response.setAge(Period.between(student.getBirthDate(), LocalDate.now()).getYears());
        }

        // 成绩统计
        calculateScoreStats(response, studentId);

        // 知识点掌握情况
        response.setKnowledgePoints(getKnowledgePointStats(studentId));

        // 错题统计
        calculateWrongStats(response, studentId);

        // 成绩趋势
        response.setScoreTrends(getScoreTrends(studentId));

        return response;
    }

    /**
     * 获取班级所有学生画像
     */
    public List<StudentProfileResponse> listClassProfiles(Long classId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getClassId, classId)
                .eq(Student::getStatus, 1)
                .orderByAsc(Student::getStudentNo);

        List<Student> students = studentMapper.selectList(wrapper);
        return students.stream()
                .map(s -> getStudentProfile(s.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取知识点掌握统计
     */
    public List<StudentProfileResponse.KnowledgePointStats> getKnowledgePointStats(Long studentId) {
        // 获取学生错题记录
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId);
        List<StudentWrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);

        // 暂时返回模拟数据，实际需要从题目表中统计知识点
        List<StudentProfileResponse.KnowledgePointStats> stats = new ArrayList<>();

        // 示例知识点统计
        String[] points = {"负数", "乘法", "加法", "圆的周长", "三角形"};
        for (String point : points) {
            StudentProfileResponse.KnowledgePointStats stat = new StudentProfileResponse.KnowledgePointStats();
            stat.setKnowledgePoint(point);
            stat.setMasteryRate(BigDecimal.valueOf(75 + Math.random() * 20).setScale(2, RoundingMode.HALF_UP));
            stat.setCorrectCount((int)(10 + Math.random() * 5));
            stat.setTotalCount(15);
            stat.setLevel(getMasteryLevel(stat.getMasteryRate()));
            stats.add(stat);
        }

        return stats;
    }

    /**
     * 获取成绩趋势
     */
    public List<StudentProfileResponse.ScoreTrend> getScoreTrends(Long studentId) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getStudentId, studentId)
                .eq(AnswerSheet::getStatus, 3) // 已批改
                .orderByDesc(AnswerSheet::getGradingTime)
                .last("LIMIT 10");

        List<AnswerSheet> answerSheets = answerSheetMapper.selectList(wrapper);

        return answerSheets.stream()
                .map(as -> {
                    StudentProfileResponse.ScoreTrend trend = new StudentProfileResponse.ScoreTrend();
                    trend.setExamPaperId(as.getExamPaperId());
                    trend.setExamName("考试" + as.getExamPaperId());
                    trend.setScore(as.getTotalScore());
                    trend.setExamDate(as.getGradingTime() != null ?
                            as.getGradingTime().toLocalDate() : LocalDate.now());
                    return trend;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取错题分析
     */
    public StudentProfileResponse getWrongAnalysis(Long studentId) {
        StudentProfileResponse response = new StudentProfileResponse();

        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId);
        List<StudentWrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);

        response.setTotalWrongCount(wrongQuestions.size());

        // 已纠错数
        int correctedCount = (int) wrongQuestions.stream()
                .filter(wq -> wq.getCorrectedAt() != null)
                .count();
        response.setCorrectedCount(correctedCount);

        // 错题类型分布（模拟）
        List<StudentProfileResponse.WrongQuestionType> types = new ArrayList<>();
        StudentProfileResponse.WrongQuestionType choiceType = new StudentProfileResponse.WrongQuestionType();
        choiceType.setQuestionType("CHOICE");
        choiceType.setTypeName("选择题");
        choiceType.setCount(wrongQuestions.size() / 2);
        choiceType.setPercentage(BigDecimal.valueOf(50));
        types.add(choiceType);

        StudentProfileResponse.WrongQuestionType fillType = new StudentProfileResponse.WrongQuestionType();
        fillType.setQuestionType("FILL");
        fillType.setTypeName("填空题");
        fillType.setCount(wrongQuestions.size() / 3);
        fillType.setPercentage(BigDecimal.valueOf(33.33));
        types.add(fillType);

        response.setWrongQuestionTypes(types);

        return response;
    }

    /**
     * 更新特长爱好
     */
    public void updateInterests(Long studentId, String interests, String talents, String learningStyle) {
        // 暂时记录日志，实际需要扩展student表或创建profile表
        log.info("更新学生特长爱好: studentId={}, interests={}, talents={}, learningStyle={}",
                studentId, interests, talents, learningStyle);
    }

    /**
     * 计算成绩统计
     */
    private void calculateScoreStats(StudentProfileResponse response, Long studentId) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getStudentId, studentId)
                .eq(AnswerSheet::getStatus, 3) // 已批改
                .isNotNull(AnswerSheet::getTotalScore);

        List<AnswerSheet> answerSheets = answerSheetMapper.selectList(wrapper);

        if (answerSheets.isEmpty()) {
            response.setExamCount(0);
            response.setAvgScore(BigDecimal.ZERO);
            return;
        }

        response.setExamCount(answerSheets.size());

        // 计算平均分
        BigDecimal total = answerSheets.stream()
                .map(AnswerSheet::getTotalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = total.divide(BigDecimal.valueOf(answerSheets.size()), 2, RoundingMode.HALF_UP);
        response.setAvgScore(avg);

        // 最高分最低分
        Optional<BigDecimal> max = answerSheets.stream()
                .map(AnswerSheet::getTotalScore)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo);
        Optional<BigDecimal> min = answerSheets.stream()
                .map(AnswerSheet::getTotalScore)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo);
        response.setHighestScore(max.orElse(BigDecimal.ZERO));
        response.setLowestScore(min.orElse(BigDecimal.ZERO));
    }

    /**
     * 计算错题统计
     */
    private void calculateWrongStats(StudentProfileResponse response, Long studentId) {
        StudentProfileResponse wrongAnalysis = getWrongAnalysis(studentId);
        response.setTotalWrongCount(wrongAnalysis.getTotalWrongCount());
        response.setCorrectedCount(wrongAnalysis.getCorrectedCount());
        response.setWrongQuestionTypes(wrongAnalysis.getWrongQuestionTypes());
    }

    /**
     * 获取掌握等级
     */
    private String getMasteryLevel(BigDecimal masteryRate) {
        if (masteryRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "优秀";
        } else if (masteryRate.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "良好";
        } else if (masteryRate.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "一般";
        } else {
            return "薄弱";
        }
    }
}