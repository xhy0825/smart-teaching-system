package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.user.entity.Clazz;
import com.edu.user.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService extends ServiceImpl<ClassMapper, Clazz> {

    @Transactional
    public Clazz createClass(Clazz clazz) {
        clazz.setStudentCount(0);
        baseMapper.insert(clazz);
        log.info("创建班级: id={}, name={}", clazz.getId(), clazz.getName());
        return clazz;
    }

    public List<Clazz> listByGrade(Long gradeId) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getGradeId, gradeId)
                .orderByAsc(Clazz::getName);
        return baseMapper.selectList(wrapper);
    }

    public Clazz getClassById(Long classId) {
        return baseMapper.selectById(classId);
    }

    @Transactional
    public void updateClass(Clazz clazz) {
        baseMapper.updateById(clazz);
        log.info("更新班级: id={}", clazz.getId());
    }

    @Transactional
    public void assignTeacher(Long classId, Long teacherId) {
        Clazz clazz = baseMapper.selectById(classId);
        if (clazz == null) {
            throw new BusinessException("班级不存在");
        }
        clazz.setTeacherId(teacherId);
        baseMapper.updateById(clazz);
        log.info("分配班主任: classId={}, teacherId={}", classId, teacherId);
    }

    @Transactional
    public void updateStudentCount(Long classId, int count) {
        Clazz clazz = baseMapper.selectById(classId);
        if (clazz != null) {
            clazz.setStudentCount(count);
            baseMapper.updateById(clazz);
        }
    }

    @Transactional
    public void deleteClass(Long classId) {
        baseMapper.deleteById(classId);
        log.info("删除班级: id={}", classId);
    }
}
