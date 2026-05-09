package com.edu.exam.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.edu.common.exception.BusinessException;
import com.edu.exam.dto.ExamGenerateRequest;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.Question;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
import com.edu.user.entity.Student;
import com.edu.user.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGenerateService {

    private final QuestionService questionService;
    private final ExamPaperService examPaperService;
    private final QuestionBankService questionBankService;
    private final StudentMapper studentMapper;
    private final StudentWrongQuestionMapper wrongQuestionMapper;

    @Transactional
    public ExamPaper generateExam(ExamGenerateRequest request) {
        // 创建试卷
        ExamPaper paper = new ExamPaper();
        paper.setTitle(request.getTitle());
        paper.setSubject(request.getSubject());
        paper.setGradeId(request.getGradeId());
        paper.setClassId(request.getClassId());
        paper.setTotalScore(BigDecimal.valueOf(request.getTotalScore()));
        paper.setTimeLimit(request.getTimeLimit());
        paper.setCreatedBy(request.getCreatedBy());

        paper = examPaperService.createPaper(paper);

        // 解析试卷结构
        JSONArray structure = JSON.parseArray(request.getStructure());
        int sequence = 1;
        int totalQuestions = 0;

        // 计算总题目数
        for (int i = 0; i < structure.size(); i++) {
            JSONObject section = structure.getJSONObject(i);
            totalQuestions += section.getIntValue("count");
        }

        // 根据生成策略选择题目
        String strategy = request.getGenerateStrategy();
        if (strategy == null) {
            strategy = "SMART";
        }

        Map<Integer, Integer> difficultyTargetCounts = calculateDifficultyDistribution(
                request.getDifficultyDistribution(), totalQuestions);

        for (int i = 0; i < structure.size(); i++) {
            JSONObject section = structure.getJSONObject(i);
            String questionType = section.getString("type");
            int count = section.getIntValue("count");
            BigDecimal scoreEach = section.getBigDecimal("scoreEach");

            // 根据策略选择题目
            List<Question> selected;
            if ("AI".equals(strategy)) {
                selected = selectQuestionsAI(request, questionType, count, difficultyTargetCounts);
            } else if ("SMART".equals(strategy)) {
                selected = selectQuestionsSmart(request, questionType, count, difficultyTargetCounts);
            } else {
                selected = selectQuestionsSimple(request, questionType, count);
            }

            // 添加到试卷
            for (Question q : selected) {
                examPaperService.addQuestionToPaper(paper.getId(), q.getId(), sequence, scoreEach);
                sequence++;
            }

            // 更新难度分布计数
            for (Question q : selected) {
                Integer diff = q.getDifficulty();
                if (diff != null && difficultyTargetCounts.containsKey(diff)) {
                    difficultyTargetCounts.put(diff, difficultyTargetCounts.get(diff) - 1);
                }
            }
        }

        log.info("生成试卷完成: paperId={}, questions={}, strategy={}", paper.getId(), sequence - 1, strategy);
        return paper;
    }

    /**
     * 计算各难度级别的题目数量分布
     */
    private Map<Integer, Integer> calculateDifficultyDistribution(
            Map<String, Double> distribution, int totalQuestions) {
        Map<Integer, Integer> result = new HashMap<>();

        if (distribution == null || distribution.isEmpty()) {
            // 默认分布：简单30%，中等50%，困难20%
            distribution = new HashMap<>();
            distribution.put("1", 0.3);
            distribution.put("2", 0.5);
            distribution.put("3", 0.2);
        }

        int assigned = 0;
        for (Map.Entry<String, Double> entry : distribution.entrySet()) {
            try {
                int difficulty = Integer.parseInt(entry.getKey());
                int count = (int) Math.round(totalQuestions * entry.getValue());
                result.put(difficulty, count);
                assigned += count;
            } catch (NumberFormatException e) {
                log.warn("Invalid difficulty key: {}", entry.getKey());
            }
        }

        // 修正总数偏差（加到中等难度）
        if (assigned != totalQuestions && result.containsKey(2)) {
            result.put(2, result.get(2) + (totalQuestions - assigned));
        }

        return result;
    }

    /**
     * 简单策略：随机选取题目
     */
    private List<Question> selectQuestionsSimple(ExamGenerateRequest request,
                                                  String questionType, int count) {
        List<Question> questions = questionService.queryQuestions(
                request.getSubject(), questionType, null, null);

        if (questions.isEmpty()) {
            throw new BusinessException("题库中没有符合条件的题目: " + questionType);
        }

        // 随机打乱后选取
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);

        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    /**
     * 智能策略：考虑难度分布和知识点覆盖
     */
    private List<Question> selectQuestionsSmart(ExamGenerateRequest request,
                                                 String questionType, int count,
                                                 Map<Integer, Integer> difficultyCounts) {
        List<Question> result = new ArrayList<>();

        // 查询所有符合条件的题目
        List<Question> allQuestions = questionService.queryQuestions(
                request.getSubject(), questionType, null, null);

        if (allQuestions.isEmpty()) {
            throw new BusinessException("题库中没有符合条件的题目: " + questionType);
        }

        // 按难度分组
        Map<Integer, List<Question>> byDifficulty = allQuestions.stream()
                .filter(q -> q.getDifficulty() != null)
                .collect(Collectors.groupingBy(Question::getDifficulty));

        // 按难度选取
        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            Integer targetCount = difficultyCounts.getOrDefault(difficulty, 0);
            if (targetCount <= 0) continue;

            List<Question> candidates = byDifficulty.getOrDefault(difficulty, new ArrayList<>());
            if (candidates.isEmpty()) continue;

            Collections.shuffle(candidates);
            int toSelect = Math.min(targetCount, Math.min(count - result.size(), candidates.size()));
            result.addAll(candidates.subList(0, toSelect));
        }

        // 如果还不够，从剩余题目中补充
        if (result.size() < count) {
            List<Question> remaining = allQuestions.stream()
                    .filter(q -> !result.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remaining);
            int needed = count - result.size();
            result.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }

        return result;
    }

    /**
     * AI策略：模拟智能推荐（实际应接入AI服务）
     */
    private List<Question> selectQuestionsAI(ExamGenerateRequest request,
                                              String questionType, int count,
                                              Map<Integer, Integer> difficultyCounts) {
        // 暂时使用智能策略，未来可接入AI服务
        log.info("AI生成试卷: studentId={}, strategy=AI", request.getTargetStudentId());

        List<Question> selected = selectQuestionsSmart(request, questionType, count, difficultyCounts);

        // AI策略额外处理：优先覆盖指定知识点
        if (request.getKnowledgePoints() != null && request.getKnowledgePoints().length > 0) {
            List<Question> allQuestions = questionService.queryQuestions(
                    request.getSubject(), questionType, null, null);

            // 按知识点匹配度排序
            List<Question> knowledgeMatched = new ArrayList<>();
            for (Question q : allQuestions) {
                if (q.getKnowledgePoints() != null) {
                    for (String kp : request.getKnowledgePoints()) {
                        if (q.getKnowledgePoints().contains(kp)) {
                            knowledgeMatched.add(q);
                            break;
                        }
                    }
                }
            }

            // 优先选择知识点匹配的题目
            Collections.shuffle(knowledgeMatched);
            int matchedCount = Math.min(count / 2, knowledgeMatched.size()); // 一半题目覆盖指定知识点
            if (matchedCount > 0 && knowledgeMatched.size() >= matchedCount) {
                // 替换部分已选题目
                for (int i = 0; i < matchedCount && i < selected.size(); i++) {
                    if (!selected.contains(knowledgeMatched.get(i))) {
                        selected.set(i, knowledgeMatched.get(i));
                    }
                }
            }
        }

        return selected;
    }

    /**
     * 根据学生画像生成个性化试卷
     * 优先覆盖学生薄弱知识点，调整难度分布
     */
    @Transactional
    public ExamPaper generatePersonalizedExam(ExamGenerateRequest request) {
        if (request.getTargetStudentId() == null) {
            throw new BusinessException("个性化出题需要指定目标学生");
        }

        // 获取学生信息
        Student student = studentMapper.selectById(request.getTargetStudentId());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 获取学生错题记录，分析薄弱知识点
        List<StudentWrongQuestion> wrongQuestions = getStudentWrongQuestions(request.getTargetStudentId());

        // 提取薄弱知识点（从错题的知识点标签中提取）
        Set<String> weakKnowledgePoints = extractWeakKnowledgePoints(wrongQuestions);

        // 自动调整知识点覆盖
        if (weakKnowledgePoints.size() > 0) {
            String[] existingKp = request.getKnowledgePoints();
            Set<String> finalKp = new HashSet<>(weakKnowledgePoints);
            if (existingKp != null) {
                finalKp.addAll(Arrays.asList(existingKp));
            }
            request.setKnowledgePoints(finalKp.toArray(new String[0]));
            log.info("个性化试卷覆盖薄弱知识点: {}", finalKp);
        }

        // 调整难度分布：根据错题情况适当降低难度
        Map<String, Double> adjustedDifficulty = adjustDifficultyForStudent(
                request.getDifficultyDistribution(), wrongQuestions.size());
        request.setDifficultyDistribution(adjustedDifficulty);

        // 使用AI策略生成试卷
        request.setGenerateStrategy("AI");

        log.info("为学生 {} 生成个性化试卷: 薄弱知识点={}, 调整难度={}",
                student.getName(), weakKnowledgePoints, adjustedDifficulty);

        return generateExam(request);
    }

    /**
     * 获取学生错题记录
     */
    private List<StudentWrongQuestion> getStudentWrongQuestions(Long studentId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentWrongQuestion> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId)
                .isNull(StudentWrongQuestion::getCorrectedAt) // 未纠错的错题
                .orderByDesc(StudentWrongQuestion::getWrongCount)
                .last("LIMIT 20");
        return wrongQuestionMapper.selectList(wrapper);
    }

    /**
     * 从错题记录中提取薄弱知识点
     */
    private Set<String> extractWeakKnowledgePoints(List<StudentWrongQuestion> wrongQuestions) {
        Set<String> knowledgePoints = new HashSet<>();

        for (StudentWrongQuestion wq : wrongQuestions) {
            // 获取题目详情
            Question question = questionService.getQuestionById(wq.getQuestionId());
            if (question != null && question.getKnowledgePoints() != null) {
                try {
                    // 解析知识点JSON
                    JSONArray kpArray = JSON.parseArray(question.getKnowledgePoints());
                    for (int i = 0; i < kpArray.size(); i++) {
                        knowledgePoints.add(kpArray.getString(i));
                    }
                } catch (Exception e) {
                    // 如果不是JSON格式，直接添加
                    knowledgePoints.add(question.getKnowledgePoints());
                }
            }
        }

        return knowledgePoints;
    }

    /**
     * 根据错题数量调整难度分布
     * 错题越多，适当增加简单题比例
     */
    private Map<String, Double> adjustDifficultyForStudent(
            Map<String, Double> original, int wrongCount) {
        Map<String, Double> adjusted = new HashMap<>();

        if (original == null || original.isEmpty()) {
            // 默认分布
            adjusted.put("1", 0.3);
            adjusted.put("2", 0.5);
            adjusted.put("3", 0.2);
        } else {
            adjusted.putAll(original);
        }

        // 根据错题数量调整：错题多则增加简单题比例
        if (wrongCount > 10) {
            // 很多错题，大幅增加简单题
            double easy = adjusted.getOrDefault("1", 0.3);
            adjusted.put("1", Math.min(0.5, easy + 0.15));
            double hard = adjusted.getOrDefault("3", 0.2);
            adjusted.put("3", Math.max(0.1, hard - 0.1));
        } else if (wrongCount > 5) {
            // 中等错题，小幅增加简单题
            double easy = adjusted.getOrDefault("1", 0.3);
            adjusted.put("1", Math.min(0.4, easy + 0.05));
        }

        // 确保总和为1
        double total = adjusted.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total != 1.0) {
            double diff = 1.0 - total;
            adjusted.put("2", adjusted.getOrDefault("2", 0.5) + diff);
        }

        return adjusted;
    }
}