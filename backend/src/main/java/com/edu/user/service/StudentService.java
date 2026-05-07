package com.edu.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.exception.BusinessException;
import com.edu.common.util.TenantContextHolder;
import com.edu.user.entity.Student;
import com.edu.user.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService extends ServiceImpl<StudentMapper, Student> {

    @Transactional
    public Student createStudent(Student student) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户上下文缺失");
        }
        student.setTenantId(tenantId);
        student.setStatus(1);
        baseMapper.insert(student);
        log.info("创建学生: id={}, name={}", student.getId(), student.getName());
        return student;
    }

    public List<Student> listByClass(Long classId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getTenantId, tenantId)
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getStudentNo);
        return baseMapper.selectList(wrapper);
    }

    public Student getStudentById(Long studentId) {
        return baseMapper.selectById(studentId);
    }

    public Student getStudentByNo(String studentNo) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getTenantId, tenantId)
                .eq(Student::getStudentNo, studentNo);
        return baseMapper.selectOne(wrapper);
    }

    @Transactional
    public void updateStudent(Student student) {
        baseMapper.updateById(student);
        log.info("更新学生: id={}", student.getId());
    }

    @Transactional
    public void transferClass(Long studentId, Long newClassId) {
        Student student = baseMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        student.setClassId(newClassId);
        baseMapper.updateById(student);
        log.info("学生转班: studentId={}, newClassId={}", studentId, newClassId);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        baseMapper.deleteById(studentId);
        log.info("删除学生: id={}", studentId);
    }
}
