package com.edu.grading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.grading.entity.AnswerSheet;
import com.edu.grading.mapper.AnswerSheetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题卡服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerSheetService extends ServiceImpl<AnswerSheetMapper, AnswerSheet> {

    @Transactional
    public AnswerSheet createAnswerSheet(Long examPaperId, Long studentId) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }

        // 检查是否已存在
        LambdaQueryWrapper<AnswerSheet> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AnswerSheet::getExamPaperId, examPaperId)
                .eq(AnswerSheet::getStudentId, studentId);
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该学生已提交答题卡");
        }

        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setTenantId(tenantId);
        answerSheet.setExamPaperId(examPaperId);
        answerSheet.setStudentId(studentId);
        answerSheet.setStatus(0);  // 未提交
        baseMapper.insert(answerSheet);

        log.info("创建答题卡: id={}, examPaperId={}, studentId={}",
                answerSheet.getId(), examPaperId, studentId);
        return answerSheet;
    }

    @Transactional
    public void submitAnswerSheet(Long answerSheetId) {
        AnswerSheet answerSheet = baseMapper.selectById(answerSheetId);
        if (answerSheet == null) {
            throw new BusinessException("答题卡不存在");
        }

        answerSheet.setStatus(1);  // 已提交
        answerSheet.setSubmitTime(LocalDateTime.now());
        baseMapper.updateById(answerSheet);

        log.info("提交答题卡: id={}", answerSheetId);
    }

    @Transactional
    public void startGrading(Long answerSheetId) {
        AnswerSheet answerSheet = baseMapper.selectById(answerSheetId);
        if (answerSheet == null) {
            throw new BusinessException("答题卡不存在");
        }

        answerSheet.setStatus(2);  // 批改中
        baseMapper.updateById(answerSheet);

        log.info("开始批改答题卡: id={}", answerSheetId);
    }

    @Transactional
    public void completeGrading(Long answerSheetId, java.math.BigDecimal totalScore, Long gradedBy) {
        AnswerSheet answerSheet = baseMapper.selectById(answerSheetId);
        if (answerSheet == null) {
            throw new BusinessException("答题卡不存在");
        }

        answerSheet.setStatus(3);  // 已批改
        answerSheet.setTotalScore(totalScore);
        answerSheet.setGradingTime(LocalDateTime.now());
        answerSheet.setGradedBy(gradedBy);
        baseMapper.updateById(answerSheet);

        log.info("批改完成: id={}, totalScore={}", answerSheetId, totalScore);
    }

    public AnswerSheet getByExamAndStudent(Long examPaperId, Long studentId) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getExamPaperId, examPaperId)
                .eq(AnswerSheet::getStudentId, studentId);
        return baseMapper.selectOne(wrapper);
    }

    public List<AnswerSheet> listByExam(Long examPaperId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getTenantId, tenantId)
                .eq(AnswerSheet::getExamPaperId, examPaperId)
                .orderByDesc(AnswerSheet::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    public AnswerSheet getById(Long id) {
        return baseMapper.selectById(id);
    }

    public int countByExam(Long examPaperId) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getExamPaperId, examPaperId);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }

    public int countGradedByExam(Long examPaperId) {
        LambdaQueryWrapper<AnswerSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerSheet::getExamPaperId, examPaperId)
                .eq(AnswerSheet::getStatus, 3);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }
}