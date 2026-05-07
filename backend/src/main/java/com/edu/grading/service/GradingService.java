package com.edu.grading.service;

import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import com.edu.exam.service.ExamPaperService;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批改服务 - 规则引擎实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {

    private final AnswerSheetService answerSheetService;
    private final AnswerService answerService;
    private final ExamPaperService examPaperService;
    private final QuestionService questionService;
    private final StudentWrongQuestionService wrongQuestionService;

    /**
     * 批改答题卡
     */
    @Transactional
    public void gradeAnswerSheet(Long answerSheetId, Long gradedBy) {
        AnswerSheet answerSheet = answerSheetService.getById(answerSheetId);
        if (answerSheet == null) {
            throw new RuntimeException("答题卡不存在");
        }

        answerSheetService.startGrading(answerSheetId);

        // 获取试卷题目
        List<ExamQuestion> examQuestions = examPaperService.getPaperQuestions(answerSheet.getExamPaperId());

        BigDecimal totalScore = BigDecimal.ZERO;

        for (ExamQuestion eq : examQuestions) {
            Answer answer = answerService.getBySheetAndQuestion(answerSheetId, eq.getId());
            if (answer == null || answer.getStudentAnswer() == null) {
                // 未作答，得0分
                answerService.updateScore(eq.getId(), BigDecimal.ZERO, 0);
                continue;
            }

            Question question = questionService.getQuestionById(eq.getQuestionId());
            if (question == null) {
                continue;
            }

            // 规则引擎批改
            GradingResult result = gradeQuestion(question, answer.getStudentAnswer(), eq.getScore());
            answerService.updateScore(answer.getId(), result.score, result.isCorrect);

            totalScore = totalScore.add(result.score);

            // 记录错题
            if (result.isCorrect == 0) {
                wrongQuestionService.recordWrongQuestion(
                        answerSheet.getStudentId(),
                        question.getId(),
                        answerSheet.getExamPaperId()
                );
            }
        }

        // 完成批改
        answerSheetService.completeGrading(answerSheetId, totalScore, gradedBy);
        log.info("批改完成: answerSheetId={}, totalScore={}", answerSheetId, totalScore);
    }

    /**
     * 规则引擎批改单题
     */
    private GradingResult gradeQuestion(Question question, String studentAnswer, BigDecimal maxScore) {
        String questionType = question.getQuestionType();
        String correctAnswer = question.getAnswer();

        GradingResult result = new GradingResult();

        switch (questionType) {
            case "CHOICE":
                result = gradeChoice(studentAnswer, correctAnswer, maxScore);
                break;
            case "FILL":
                result = gradeFill(studentAnswer, correctAnswer, maxScore);
                break;
            case "JUDGE":
                result = gradeJudge(studentAnswer, correctAnswer, maxScore);
                break;
            case "CALCULATION":
                result = gradeCalculation(studentAnswer, correctAnswer, maxScore);
                break;
            default:
                result.score = BigDecimal.ZERO;
                result.isCorrect = 0;
        }

        return result;
    }

    /**
     * 选择题批改 - 精确匹配
     */
    private GradingResult gradeChoice(String studentAnswer, String correctAnswer, BigDecimal maxScore) {
        String normalizedStudent = normalize(studentAnswer);
        String normalizedCorrect = normalize(correctAnswer);

        GradingResult result = new GradingResult();
        if (normalizedStudent.equals(normalizedCorrect)) {
            result.score = maxScore;
            result.isCorrect = 1;
        } else {
            result.score = BigDecimal.ZERO;
            result.isCorrect = 0;
        }
        return result;
    }

    /**
     * 填空题批改 - 多答案/模糊匹配
     */
    private GradingResult gradeFill(String studentAnswer, String correctAnswer, BigDecimal maxScore) {
        String normalizedStudent = normalize(studentAnswer);
        String normalizedCorrect = normalize(correctAnswer);

        GradingResult result = new GradingResult();

        // 支持多答案（用|分隔）
        String[] correctOptions = normalizedCorrect.split("\\|");
        for (String option : correctOptions) {
            if (normalizedStudent.equals(option.trim())) {
                result.score = maxScore;
                result.isCorrect = 1;
                return result;
            }
        }

        // 模糊匹配（允许空格差异）
        if (normalizedStudent.replaceAll(" ", "").equals(normalizedCorrect.replaceAll(" ", ""))) {
            result.score = maxScore;
            result.isCorrect = 1;
        } else {
            result.score = BigDecimal.ZERO;
            result.isCorrect = 0;
        }
        return result;
    }

    /**
     * 判断题批改 - 布尔值匹配
     */
    private GradingResult gradeJudge(String studentAnswer, String correctAnswer, BigDecimal maxScore) {
        String normalizedStudent = normalize(studentAnswer);
        String normalizedCorrect = normalize(correctAnswer);

        // 转换为统一格式
        boolean studentBool = parseBoolean(normalizedStudent);
        boolean correctBool = parseBoolean(normalizedCorrect);

        GradingResult result = new GradingResult();
        if (studentBool == correctBool) {
            result.score = maxScore;
            result.isCorrect = 1;
        } else {
            result.score = BigDecimal.ZERO;
            result.isCorrect = 0;
        }
        return result;
    }

    /**
     * 计算题批改 - 数值答案匹配
     */
    private GradingResult gradeCalculation(String studentAnswer, String correctAnswer, BigDecimal maxScore) {
        GradingResult result = new GradingResult();

        try {
            BigDecimal studentValue = new BigDecimal(normalize(studentAnswer));
            BigDecimal correctValue = new BigDecimal(normalize(correctAnswer));

            // 允许0.01误差
            if (studentValue.subtract(correctValue).abs().compareTo(new BigDecimal("0.01")) <= 0) {
                result.score = maxScore;
                result.isCorrect = 1;
            } else {
                result.score = BigDecimal.ZERO;
                result.isCorrect = 0;
            }
        } catch (NumberFormatException e) {
            // 无法解析为数值，尝试精确匹配
            return gradeFill(studentAnswer, correctAnswer, maxScore);
        }

        return result;
    }

    /**
     * 标准化答案字符串
     */
    private String normalize(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * 解析布尔值
     */
    private boolean parseBoolean(String value) {
        return "TRUE".equals(value) || "T".equals(value) || "正确".equals(value) || "对".equals(value) || "1".equals(value);
    }

    /**
     * 批改结果
     */
    private static class GradingResult {
        BigDecimal score;
        int isCorrect;  // 0-错误, 1-正确, 2-部分正确
    }
}