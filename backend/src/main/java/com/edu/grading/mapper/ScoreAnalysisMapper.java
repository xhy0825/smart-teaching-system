package com.edu.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.grading.entity.ScoreAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩分析Mapper
 */
@Mapper
public interface ScoreAnalysisMapper extends BaseMapper<ScoreAnalysis> {
}