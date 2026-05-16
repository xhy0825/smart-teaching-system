package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
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
    private final QuestionService questionService;

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

        // 知识点雷达图
        response.setKnowledgeRadar(buildKnowledgeRadar(studentId));

        // 错题类型饼图
        response.setWrongTypePie(buildWrongTypePie(studentId));

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

        // 按知识点聚合
        Map<String, KnowledgeAgg> aggMap = new HashMap<>();

        for (StudentWrongQuestion wq : wrongQuestions) {
            Question question = questionService.getById(wq.getQuestionId());
            if (question != null && question.getKnowledgePoints() != null) {
                try {
                    JSONArray points = JSON.parseArray(question.getKnowledgePoints().toString());
                    if (points != null) {
                        for (int i = 0; i < points.size(); i++) {
                            String point = points.getString(i);
                            aggMap.computeIfAbsent(point, k -> new KnowledgeAgg()).incrementTotal();
                        }
                    }
                } catch (Exception e) {
                    // 解析失败跳过
                }
            }
        }

        // 获取学生总的答题记录（用于计算正确数）
        List<StudentProfileResponse.KnowledgePointStats> stats = new ArrayList<>();
        for (Map.Entry<String, KnowledgeAgg> entry : aggMap.entrySet()) {
            StudentProfileResponse.KnowledgePointStats stat = new StudentProfileResponse.KnowledgePointStats();
            stat.setKnowledgePoint(entry.getKey());
            int total = entry.getValue().getTotalCount();
            // 假设错误数为1，正确数 = total - 1（简化逻辑）
            int wrongCount = 1;
            int correctCount = Math.max(0, total - wrongCount);
            stat.setCorrectCount(correctCount);
            stat.setTotalCount(total);
            BigDecimal masteryRate = total > 0 ?
                    BigDecimal.valueOf((correctCount * 100.0) / total).setScale(2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            stat.setMasteryRate(masteryRate);
            stat.setLevel(getMasteryLevel(masteryRate));
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

    /**
     * 构建知识点雷达图数据
     */
    public StudentProfileResponse.KnowledgeRadar buildKnowledgeRadar(Long studentId) {
        List<StudentProfileResponse.KnowledgePointStats> stats = getKnowledgePointStats(studentId);
        StudentProfileResponse.KnowledgeRadar radar = new StudentProfileResponse.KnowledgeRadar();

        List<String> points = new ArrayList<>();
        List<BigDecimal> scores = new ArrayList<>();

        for (StudentProfileResponse.KnowledgePointStats stat : stats) {
            points.add(stat.getKnowledgePoint());
            scores.add(stat.getMasteryRate());
        }

        radar.setPoints(points);
        radar.setScores(scores);
        return radar;
    }

    /**
     * 构建错题类型饼图数据
     */
    public StudentProfileResponse.WrongTypePie buildWrongTypePie(Long studentId) {
        // 查询学生错题
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId);
        List<StudentWrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);

        // 按题型聚合
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (StudentWrongQuestion wq : wrongQuestions) {
            Question question = questionService.getById(wq.getQuestionId());
            if (question != null) {
                String type = question.getQuestionType();
                typeCountMap.merge(type, 1, Integer::sum);
            }
        }

        StudentProfileResponse.WrongTypePie pie = new StudentProfileResponse.WrongTypePie();
        List<String> types = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 题型名称映射
        Map<String, String> typeNameMap = Map.of(
                "CHOICE", "选择题",
                "FILL", "填空题",
                "JUDGE", "判断题",
                "ESSAY", "简答题"
        );

        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            types.add(typeNameMap.getOrDefault(entry.getKey(), entry.getKey()));
            counts.add(entry.getValue());
        }

        pie.setTypes(types);
        pie.setCounts(counts);
        return pie;
    }

    /**
     * 知识点聚合内部类
     */
    private static class KnowledgeAgg {
        private int totalCount = 0;

        void incrementTotal() {
            totalCount++;
        }

        int getTotalCount() { return totalCount; }
    }
}