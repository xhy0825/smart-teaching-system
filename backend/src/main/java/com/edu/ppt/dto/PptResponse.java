package com.edu.ppt.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PptResponse {
    private Long id;
    private Long tenantId;
    private String title;
    private String subject;
    private String templateType;
    private String templateUrl;     // 生生的PPT文件路径
    private Integer pageCount;      // 页数
    private List<PptSlideResponse> slides;
    private Long createdBy;
    private LocalDateTime createdAt;
}