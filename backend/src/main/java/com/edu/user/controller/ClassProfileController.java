package com.edu.user.controller;

import com.edu.common.entity.Result;
import com.edu.user.dto.ClassProfileStatsResponse;
import com.edu.user.service.ClassProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class-profile")
@RequiredArgsConstructor
public class ClassProfileController {

    private final ClassProfileService classProfileService;

    /**
     * 获取班级画像统计
     */
    @GetMapping("/{classId}/stats")
    public Result<ClassProfileStatsResponse> getClassStats(@PathVariable Long classId) {
        ClassProfileStatsResponse stats = classProfileService.getClassStats(classId);
        return Result.success(stats);
    }
}
