package com.edu.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.grading.entity.AnswerSheet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题卡Mapper
 */
@Mapper
public interface AnswerSheetMapper extends BaseMapper<AnswerSheet> {
}