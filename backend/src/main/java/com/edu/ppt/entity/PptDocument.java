package com.edu.ppt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ppt_document")
public class PptDocument {
    private Long id;
    private Long tenantId;
    private String title;
    private String subject;
    private String templateType;    // LESSON/EXAM/SUMMARY
    private String contentJson;     // 内容JSON
    private String filePath;        // 生成的文件路径
    private Integer pageCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}