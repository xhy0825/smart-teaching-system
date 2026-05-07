package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.exam.entity.Question;
import com.edu.exam.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {

    @Transactional
    public Question createQuestion(Question question) {
        question.setSource("MANUAL");
        baseMapper.insert(question);
        log.info("创建题目: id={}, type={}", question.getId(), question.getQuestionType());
        return question;
    }

    public List<Question> listByBank(Long bankId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getBankId, bankId)
                .orderByAsc(Question::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public List<Question> queryQuestions(String subject, String questionType,
                                          Integer difficulty, String knowledgePoint) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSubject, subject);
        if (questionType != null) {
            wrapper.eq(Question::getQuestionType, questionType);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (knowledgePoint != null) {
            wrapper.like(Question::getKnowledgePoints, knowledgePoint);
        }
        return baseMapper.selectList(wrapper);
    }

    public Question getQuestionById(Long questionId) {
        return baseMapper.selectById(questionId);
    }

    @Transactional
    public void updateQuestion(Question question) {
        baseMapper.updateById(question);
        log.info("更新题目: id={}", question.getId());
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        baseMapper.deleteById(questionId);
        log.info("删除题目: id={}", questionId);
    }

    public int countByBank(Long bankId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getBankId, bankId);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }
}
