package com.edu.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.grading.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题详情Mapper
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
}