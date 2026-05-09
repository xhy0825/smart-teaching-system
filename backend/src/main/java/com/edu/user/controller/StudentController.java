package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.entity.Student;
import com.edu.user.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/class/{classId}")
    public Result<List<Student>> listByClass(@PathVariable Long classId) {
        List<Student> students = studentService.listByClass(classId);
        return Result.success(students);
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    @GetMapping("/no/{studentNo}")
    public Result<Student> getByNo(@PathVariable String studentNo) {
        Student student = studentService.getStudentByNo(studentNo);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    @PostMapping
    public Result<Student> create(@RequestBody Student student) {
        Student created = studentService.createStudent(student);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return Result.success();
    }

    @PutMapping("/{id}/transfer")
    public Result<Void> transferClass(@PathVariable Long id, @RequestParam Long newClassId) {
        studentService.transferClass(id, newClassId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }
}