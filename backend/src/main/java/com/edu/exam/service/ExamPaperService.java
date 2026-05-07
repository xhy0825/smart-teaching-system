package com.edu.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.mapper.ExamPaperMapper;
import com.edu.exam.mapper.ExamQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamPaperService extends ServiceImpl<ExamPaperMapper, ExamPaper> {

    private final ExamQuestionMapper examQuestionMapper;

    @Transactional
    public ExamPaper createPaper(ExamPaper paper) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }
        paper.setTenantId(tenantId);
        paper.setStatus(0); // 草稿
        baseMapper.insert(paper);
        log.info("创建试卷: id={}, title={}", paper.getId(), paper.getTitle());
        return paper;
    }

    public List<ExamPaper> listByTenant() {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getTenantId, tenantId)
                .orderByDesc(ExamPaper::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public List<ExamPaper> listByClass(Long classId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getTenantId, tenantId)
                .eq(ExamPaper::getClassId, classId)
                .orderByDesc(ExamPaper::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public ExamPaper getPaperById(Long paperId) {
        return baseMapper.selectById(paperId);
    }

    @Transactional
    public void updatePaper(ExamPaper paper) {
        baseMapper.updateById(paper);
        log.info("更新试卷: id={}", paper.getId());
    }

    @Transactional
    public void publishPaper(Long paperId) {
        ExamPaper paper = baseMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        paper.setStatus(1); // 已发布
        paper.setPublishedAt(LocalDateTime.now());
        baseMapper.updateById(paper);
        log.info("发布试卷: id={}", paperId);
    }

    @Transactional
    public void deletePaper(Long paperId) {
        baseMapper.deleteById(paperId);
        // 删除关联题目
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getExamPaperId, paperId);
        examQuestionMapper.delete(wrapper);
        log.info("删除试卷: id={}", paperId);
    }

    @Transactional
    public void addQuestionToPaper(Long paperId, Long questionId, Integer sequence, BigDecimal score) {
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamPaperId(paperId);
        examQuestion.setQuestionId(questionId);
        examQuestion.setSequence(sequence);
        examQuestion.setScore(score);
        examQuestionMapper.insert(examQuestion);
        log.info("添加题目到试卷: paperId={}, questionId={}, seq={}", paperId, questionId, sequence);
    }

    public List<ExamQuestion> getPaperQuestions(Long paperId) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getExamPaperId, paperId)
                .orderByAsc(ExamQuestion::getSequence);
        return examQuestionMapper.selectList(wrapper);
    }
}
