package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.grading.entity.StudentWrongQuestion;
import com.edu.grading.mapper.StudentWrongQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生错题记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentWrongQuestionService extends ServiceImpl<StudentWrongQuestionMapper, StudentWrongQuestion> {

    /**
     * 记录错题
     */
    @Transactional
    public void recordWrongQuestion(Long studentId, Long questionId, Long examPaperId) {
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId)
                .eq(StudentWrongQuestion::getQuestionId, questionId);
        StudentWrongQuestion existing = baseMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongAt(LocalDateTime.now());
            existing.setExamPaperId(examPaperId);
            existing.setCorrectedAt(null);  // 重置纠错标记
            baseMapper.updateById(existing);
            log.info("更新错题记录: studentId={}, questionId={}, wrongCount={}",
                    studentId, questionId, existing.getWrongCount());
        } else {
            StudentWrongQuestion wrongQuestion = new StudentWrongQuestion();
            wrongQuestion.setStudentId(studentId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setExamPaperId(examPaperId);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongAt(LocalDateTime.now());
            baseMapper.insert(wrongQuestion);
            log.info("新增错题记录: studentId={}, questionId={}", studentId, questionId);
        }
    }

    /**
     * 标记已纠错
     */
    @Transactional
    public void markCorrected(Long studentId, Long questionId) {
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId)
                .eq(StudentWrongQuestion::getQuestionId, questionId);
        StudentWrongQuestion existing = baseMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setCorrectedAt(LocalDateTime.now());
            baseMapper.updateById(existing);
            log.info("标记纠错: studentId={}, questionId={}", studentId, questionId);
        }
    }

    /**
     * 获取学生错题列表
     */
    public List<StudentWrongQuestion> listByStudent(Long studentId) {
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId)
                .isNull(StudentWrongQuestion::getCorrectedAt)  // 未纠错的
                .orderByDesc(StudentWrongQuestion::getLastWrongAt);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 获取高频错题
     */
    public List<StudentWrongQuestion> listFrequentWrong(Long limit) {
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(StudentWrongQuestion::getWrongCount, 3)
                .isNull(StudentWrongQuestion::getCorrectedAt)
                .orderByDesc(StudentWrongQuestion::getWrongCount)
                .last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 统计学生错题数
     */
    public int countByStudent(Long studentId) {
        LambdaQueryWrapper<StudentWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentWrongQuestion::getStudentId, studentId)
                .isNull(StudentWrongQuestion::getCorrectedAt);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }
}