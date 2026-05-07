package com.edu.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.exam.entity.ExamTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷模板 Mapper
 */
@Mapper
public interface ExamTemplateMapper extends BaseMapper<ExamTemplate> {
}
