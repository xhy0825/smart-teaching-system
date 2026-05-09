package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.dto.StudentProfileResponse;
import com.edu.user.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生画像控制器
 */
@RestController
@RequestMapping("/api/student-profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    /**
     * 获取学生画像详情
     */
    @GetMapping("/{studentId}")
    public Result<StudentProfileResponse> getProfile(@PathVariable Long studentId) {
        StudentProfileResponse profile = studentProfileService.getStudentProfile(studentId);
        if (profile == null) {
            return Result.error("学生不存在");
        }
        return Result.success(profile);
    }

    /**
     * 获取班级所有学生画像列表
     */
    @GetMapping("/class/{classId}")
    public Result<List<StudentProfileResponse>> listByClass(@PathVariable Long classId) {
        List<StudentProfileResponse> profiles = studentProfileService.listClassProfiles(classId);
        return Result.success(profiles);
    }

    /**
     * 获取学生知识点掌握情况
     */
    @GetMapping("/{studentId}/knowledge-points")
    public Result<List<StudentProfileResponse.KnowledgePointStats>> getKnowledgePoints(@PathVariable Long studentId) {
        List<StudentProfileResponse.KnowledgePointStats> stats = studentProfileService.getKnowledgePointStats(studentId);
        return Result.success(stats);
    }

    /**
     * 获取学生成绩趋势
     */
    @GetMapping("/{studentId}/score-trends")
    public Result<List<StudentProfileResponse.ScoreTrend>> getScoreTrends(@PathVariable Long studentId) {
        List<StudentProfileResponse.ScoreTrend> trends = studentProfileService.getScoreTrends(studentId);
        return Result.success(trends);
    }

    /**
     * 获取学生错题分析
     */
    @GetMapping("/{studentId}/wrong-analysis")
    public Result<StudentProfileResponse> getWrongAnalysis(@PathVariable Long studentId) {
        StudentProfileResponse analysis = studentProfileService.getWrongAnalysis(studentId);
        return Result.success(analysis);
    }

    /**
     * 更新学生特长爱好
     */
    @PutMapping("/{studentId}/interests")
    public Result<Void> updateInterests(
            @PathVariable Long studentId,
            @RequestParam String interests,
            @RequestParam(required = false) String talents,
            @RequestParam(required = false) String learningStyle) {
        studentProfileService.updateInterests(studentId, interests, talents, learningStyle);
        return Result.success();
    }
}