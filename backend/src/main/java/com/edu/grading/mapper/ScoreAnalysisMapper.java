package com.edu.grading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.grading.entity.ScoreAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 成绩分析Mapper
 */
@Mapper
public interface ScoreAnalysisMapper extends BaseMapper<ScoreAnalysis> {

    @Select("SELECT * FROM score_analysis WHERE class_id = #{classId} ORDER BY created_at DESC LIMIT 1")
    ScoreAnalysis selectLatestByClassId(@Param("classId") Long classId);
}