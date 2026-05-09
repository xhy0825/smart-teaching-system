package com.edu.ppt.controller;

import com.edu.common.entity.Result;
import com.edu.ppt.dto.PptGenerateRequest;
import com.edu.ppt.dto.PptResponse;
import com.edu.ppt.service.PptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ppt")
@RequiredArgsConstructor
public class PptController {

    private final PptService pptService;

    @PostMapping("/generate")
    public Result<PptResponse> generatePpt(@RequestBody PptGenerateRequest request) {
        PptResponse response = pptService.generatePpt(request);
        return Result.success(response);
    }

    @GetMapping
    public Result<List<PptResponse>> listPpts() {
        List<PptResponse> ppts = pptService.listByTenant();
        return Result.success(ppts);
    }

    @GetMapping("/{id}")
    public Result<PptResponse> getPpt(@PathVariable Long id) {
        PptResponse ppt = pptService.getById(id);
        if (ppt == null) {
            return Result.error("PPT不存在");
        }
        return Result.success(ppt);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePpt(@PathVariable Long id) {
        pptService.delete(id);
        return Result.success();
    }
}