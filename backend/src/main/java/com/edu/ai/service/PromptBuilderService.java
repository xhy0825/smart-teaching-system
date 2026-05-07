package com.edu.ai.service;

import com.edu.ai.dto.QuestionGenerateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI Prompt构建服务
 */
@Slf4j
@Service
public class PromptBuilderService {

    /**
     * 构建题目生成Prompt
     */
    public String buildQuestionPrompt(QuestionGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();

        // 基础信息
        prompt.append("请生成").append(request.getCount()).append("道");
        prompt.append(getSubjectCN(request.getSubject()));
        prompt.append(getTypeCN(request.getQuestionType()));
        prompt.append("，难度为").append(getDifficultyCN(request.getDifficulty())).append("。\n\n");

        // 知识点
        if (request.getKnowledgePoint() != null && !request.getKnowledgePoint().isEmpty()) {
            prompt.append("知识点范围：").append(request.getKnowledgePoint()).append("\n\n");
        }

        // 额外要求
        if (request.getAdditionalRequirements() != null) {
            prompt.append("特殊要求：").append(request.getAdditionalRequirements()).append("\n\n");
        }

        // 格式要求
        prompt.append("请严格按照以下格式返回，确保JSON格式正确：\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"content\": \"题目内容\",\n");

        if ("CHOICE".equals(request.getQuestionType())) {
            prompt.append("    \"options\": {\"A\":\"选项A\",\"B\":\"选项B\",\"C\":\"选项C\",\"D\":\"选项D\"},\n");
        }

        prompt.append("    \"answer\": \"标准答案\",\n");
        prompt.append("    \"answerAnalysis\": \"答案解析\",\n");
        prompt.append("    \"difficulty\": ").append(request.getDifficulty()).append(",\n");
        prompt.append("    \"knowledgePoints\": [\"知识点1\", \"知识点2\"]\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");

        prompt.append("注意：\n");
        prompt.append("1. 题目内容要准确、规范\n");
        prompt.append("2. 答案要明确、唯一\n");
        prompt.append("3. 解析要清晰易懂\n");
        prompt.append("4. 知识点要具体\n");
        prompt.append("5. 只返回JSON数组，不要有任何额外文字\n");

        return prompt.toString();
    }

    /**
     * 构建主观题批改Prompt
     */
    public String buildGradingPrompt(String questionContent, String correctAnswer,
                                      String studentAnswer, double maxScore) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请批改以下主观题：\n\n");
        prompt.append("【题目】\n").append(questionContent).append("\n\n");
        prompt.append("【标准答案】\n").append(correctAnswer).append("\n\n");
        prompt.append("【学生答案】\n").append(studentAnswer).append("\n\n");
        prompt.append("【满分】").append(maxScore).append("分\n\n");

        prompt.append("请按以下标准评分并返回JSON格式结果：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 得分(0到").append(maxScore).append("之间的数字),\n");
        prompt.append("  \"isCorrect\": 评分结果(0-错误,1-正确,2-部分正确),\n");
        prompt.append("  \"analysis\": \"批改分析(指出错误之处和正确答案)\"\n");
        prompt.append("}\n\n");

        prompt.append("评分标准：\n");
        prompt.append("- 完全正确：满分，isCorrect=1\n");
        prompt.append("- 部分正确：按比例给分，isCorrect=2\n");
        prompt.append("- 完全错误：0分，isCorrect=0\n");
        prompt.append("- 只返回JSON，不要有其他文字\n");

        return prompt.toString();
    }

    private String getSubjectCN(String subject) {
        switch (subject) {
            case "MATH": return "数学";
            case "PHYSICS": return "物理";
            case "CHEMISTRY": return "化学";
            case "ENGLISH": return "英语";
            default: return subject;
        }
    }

    private String getTypeCN(String type) {
        switch (type) {
            case "CHOICE": return "选择题";
            case "FILL": return "填空题";
            case "JUDGE": return "判断题";
            case "CALCULATION": return "计算题";
            case "SHORT_ANSWER": return "简答题";
            default: return type;
        }
    }

    private String getDifficultyCN(Integer difficulty) {
        switch (difficulty) {
            case 1: return "简单（基础概念）";
            case 2: return "中等（综合应用）";
            case 3: return "困难（创新拓展）";
            default: return "中等";
        }
    }
}