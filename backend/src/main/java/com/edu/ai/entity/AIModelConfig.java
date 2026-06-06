package com.edu.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 模型配置实体
 * 对应表：ai_model_config
 */
@Data
@TableName("ai_model_config")
public class AIModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，0表示系统默认
     */
    private Long tenantId;

    /**
     * 供应商：CLAUDE, DEEPSEEK, OPENAI, QWEN 等
     */
    private String provider;

    /**
     * 显示名称
     */
    private String providerName;

    /**
     * API 地址
     */
    private String apiUrl;

    /**
     * API Key（加密存储）
     */
    private String apiKey;

    /**
     * 默认模型
     */
    private String model;

    /**
     * 可用模型列表（JSON 数组）
     */
    private String availableModels;

    /**
     * 最大 Token 数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private BigDecimal temperature;

    /**
     * 是否默认（每租户唯一）
     */
    private Boolean isDefault;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
