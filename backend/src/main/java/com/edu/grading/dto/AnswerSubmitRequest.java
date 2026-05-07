package com.edu.grading.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 答题提交请求
 */
@Data
public class AnswerSubmitRequest {

    @NotNull(message = "答题卡ID不能为空")
    private Long answerSheetId;

    @NotNull(message = "试卷题目ID不能为空")
    private Long examQuestionId;

    private String studentAnswer;
}