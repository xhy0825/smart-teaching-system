package com.edu.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.grading.entity.StudentWrongQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生错题记录Mapper
 */
@Mapper
public interface StudentWrongQuestionMapper extends BaseMapper<StudentWrongQuestion> {
}