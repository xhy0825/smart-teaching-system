package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.exam.entity.Question;
import com.edu.exam.service.QuestionService;
import com.edu.grading.entity.Answer;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.mapper.AnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService extends ServiceImpl<AnswerMapper, Answer> {

    private final QuestionService questionService;

    @Transactional
    public Answer saveAnswer(Long answerSheetId, Long examQuestionId, String studentAnswer) {
        // 检查是否已存在答案
        LambdaQueryWrapper<Answer> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Answer::getAnswerSheetId, answerSheetId)
                .eq(Answer::getExamQuestionId, examQuestionId);
        Answer existing = baseMapper.selectOne(checkWrapper);

        if (existing != null) {
            existing.setStudentAnswer(studentAnswer);
            baseMapper.updateById(existing);
            log.info("更新答案: answerSheetId={}, examQuestionId={}", answerSheetId, examQuestionId);
            return existing;
        }

        Answer answer = new Answer();
        answer.setAnswerSheetId(answerSheetId);
        answer.setExamQuestionId(examQuestionId);
        answer.setStudentAnswer(studentAnswer);
        baseMapper.insert(answer);

        log.info("保存答案: answerSheetId={}, examQuestionId={}", answerSheetId, examQuestionId);
        return answer;
    }

    public List<Answer> listByAnswerSheet(Long answerSheetId) {
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getAnswerSheetId, answerSheetId);
        return baseMapper.selectList(wrapper);
    }

    public Answer getBySheetAndQuestion(Long answerSheetId, Long examQuestionId) {
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getAnswerSheetId, answerSheetId)
                .eq(Answer::getExamQuestionId, examQuestionId);
        return baseMapper.selectOne(wrapper);
    }

    @Transactional
    public void updateScore(Long answerId, BigDecimal score, Integer isCorrect) {
        Answer answer = baseMapper.selectById(answerId);
        if (answer == null) {
            return;
        }

        answer.setScore(score);
        answer.setIsCorrect(isCorrect);
        answer.setGradedAt(LocalDateTime.now());
        baseMapper.updateById(answer);
    }

    @Transactional
    public void setAiScore(Long answerId, BigDecimal aiScore, String aiAnalysis) {
        Answer answer = baseMapper.selectById(answerId);
        if (answer == null) {
            return;
        }

        answer.setAiScore(aiScore);
        answer.setAiAnalysis(aiAnalysis);
        baseMapper.updateById(answer);
    }

    @Transactional
    public void setManualScore(Long answerId, BigDecimal manualScore) {
        Answer answer = baseMapper.selectById(answerId);
        if (answer == null) {
            return;
        }

        answer.setManualScore(manualScore);
        answer.setGradedAt(LocalDateTime.now());
        // 人工评分优先
        answer.setScore(manualScore);
        baseMapper.updateById(answer);
    }
}