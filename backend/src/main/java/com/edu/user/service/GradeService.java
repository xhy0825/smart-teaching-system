package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.user.entity.Grade;
import com.edu.user.mapper.GradeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService extends ServiceImpl<GradeMapper, Grade> {

    @Transactional
    public Grade createGrade(Grade grade) {
        baseMapper.insert(grade);
        log.info("创建年级: id={}, name={}", grade.getId(), grade.getName());
        return grade;
    }

    public List<Grade> listBySchool(Long schoolId) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Grade::getSchoolId, schoolId)
                .orderByAsc(Grade::getSequence);
        return baseMapper.selectList(wrapper);
    }

    public Grade getGradeById(Long gradeId) {
        return baseMapper.selectById(gradeId);
    }

    @Transactional
    public void updateGrade(Grade grade) {
        baseMapper.updateById(grade);
        log.info("更新年级: id={}", grade.getId());
    }

    @Transactional
    public void deleteGrade(Long gradeId) {
        baseMapper.deleteById(gradeId);
        log.info("删除年级: id={}", gradeId);
    }
}
