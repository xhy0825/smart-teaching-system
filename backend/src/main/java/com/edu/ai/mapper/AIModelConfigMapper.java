package com.edu.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.ai.entity.AIModelConfig;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * AI 模型配置 Mapper
 */
public interface AIModelConfigMapper extends BaseMapper<AIModelConfig> {

    /**
     * 根据租户ID查询配置列表
     */
    List<AIModelConfig> selectByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 获取租户的默认配置
     */
    AIModelConfig selectDefaultByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 清除租户的默认配置标记
     */
    void clearDefaultFlag(@Param("tenantId") Long tenantId);

    /**
     * 设置默认配置
     */
    void setDefault(@Param("id") Long id);
}
