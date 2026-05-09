package com.edu.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.common.entity.Result;
import com.edu.user.entity.Clazz;
import com.edu.user.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassMapper classMapper;

    @GetMapping("/list")
    public Result<List<Clazz>> listAll() {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Clazz::getGradeId).orderByAsc(Clazz::getName);
        List<Clazz> classes = classMapper.selectList(wrapper);
        return Result.success(classes);
    }

    @GetMapping("/grade/{gradeId}")
    public Result<List<Clazz>> listByGrade(@PathVariable Long gradeId) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getGradeId, gradeId)
                .orderByAsc(Clazz::getName);
        List<Clazz> classes = classMapper.selectList(wrapper);
        return Result.success(classes);
    }

    @GetMapping("/{id}")
    public Result<Clazz> getById(@PathVariable Long id) {
        Clazz clazz = classMapper.selectById(id);
        if (clazz == null) {
            return Result.error("班级不存在");
        }
        return Result.success(clazz);
    }

    @PostMapping
    public Result<Clazz> create(@RequestBody Clazz clazz) {
        clazz.setStudentCount(0);
        classMapper.insert(clazz);
        return Result.success(clazz);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Clazz clazz) {
        clazz.setId(id);
        classMapper.updateById(clazz);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        classMapper.deleteById(id);
        return Result.success();
    }
}