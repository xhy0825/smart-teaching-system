package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.entity.Grade;
import com.edu.user.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/list")
    public Result<List<Grade>> listAll() {
        List<Grade> grades = gradeService.listBySchool(1L);
        return Result.success(grades);
    }

    @GetMapping("/school/{schoolId}")
    public Result<List<Grade>> listBySchool(@PathVariable Long schoolId) {
        List<Grade> grades = gradeService.listBySchool(schoolId);
        return Result.success(grades);
    }

    @GetMapping("/{id}")
    public Result<Grade> getById(@PathVariable Long id) {
        Grade grade = gradeService.getById(id);
        if (grade == null) {
            return Result.error("年级不存在");
        }
        return Result.success(grade);
    }
}