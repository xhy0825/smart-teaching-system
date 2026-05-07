package com.edu.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.exam.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷 Mapper
 */
@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {
}
