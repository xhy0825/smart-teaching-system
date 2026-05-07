package com.edu.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.exam.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷题目关联 Mapper
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
}
