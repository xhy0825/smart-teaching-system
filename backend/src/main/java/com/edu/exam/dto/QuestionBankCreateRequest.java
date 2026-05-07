package com.edu.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 题库创建请求
 */
@Data
public class QuestionBankCreateRequest {

    @NotBlank(message = "题库名称不能为空")
    @Size(max = 100, message = "题库名称最长100字符")
    private String name;

    @NotBlank(message = "学科不能为空")
    private String subject;

    private Integer gradeLevel;

    @Size(max = 200, message = "描述最长200字符")
    private String description;

    private Integer isPublic;

    private Long createdBy;
}