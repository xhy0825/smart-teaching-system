package com.edu.exam.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.edu.common.exception.BusinessException;
import com.edu.exam.dto.ExamGenerateRequest;
import com.edu.exam.entity.ExamPaper;
import com.edu.exam.entity.ExamQuestion;
import com.edu.exam.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGenerateService {

    private final QuestionService questionService;
    private final ExamPaperService examPaperService;
    private final QuestionBankService questionBankService;

    @Transactional
    public ExamPaper generateExam(ExamGenerateRequest request) {
        // 创建试卷
        ExamPaper paper = new ExamPaper();
        paper.setTitle(request.getTitle());
        paper.setSubject(request.getSubject());
        paper.setGradeId(request.getGradeId());
        paper.setClassId(request.getClassId());
        paper.setTotalScore(BigDecimal.valueOf(request.getTotalScore()));
        paper.setTimeLimit(request.getTimeLimit());
        paper.setCreatedBy(request.getCreatedBy());

        paper = examPaperService.createPaper(paper);

        // 解析试卷结构
        JSONArray structure = JSON.parseArray(request.getStructure());
        int sequence = 1;

        for (int i = 0; i < structure.size(); i++) {
            JSONObject section = structure.getJSONObject(i);
            String questionType = section.getString("type");
            int count = section.getIntValue("count");
            BigDecimal scoreEach = section.getBigDecimal("scoreEach");

            // 查询符合条件的题目
            List<Question> questions = questionService.queryQuestions(
                    request.getSubject(), questionType, null, null);

            // 选取题目（简单实现：随机选取）
            List<Question> selected = selectQuestions(questions, count);

            // 添加到试卷
            for (Question q : selected) {
                examPaperService.addQuestionToPaper(paper.getId(), q.getId(), sequence, scoreEach);
                sequence++;
            }
        }

        log.info("生成试卷完成: paperId={}, questions={}", paper.getId(), sequence - 1);
        return paper;
    }

    private List<Question> selectQuestions(List<Question> questions, int count) {
        if (questions.size() <= count) {
            return new ArrayList<>(questions);
        }
        // 简单实现：取前N个
        return questions.subList(0, count);
    }
}
