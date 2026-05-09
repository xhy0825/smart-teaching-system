package com.edu.ppt.dto;

import lombok.Data;

import java.util.List;

@Data
public class PptGenerateRequest {
    private String title;           // PPT标题
    private String subject;         // 学科
    private Long gradeId;           // 年级ID
    private String templateType;    // 模板类型 LESSON/EXAM/SUMMARY
    private List<Long> questionIds; // 包含的题目ID列表
    private List<String> knowledgePoints; // 知识点列表
    private String customContent;   // 自定义内容
    private Long createdBy;         // 创建者ID
}