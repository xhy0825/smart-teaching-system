-- 创建 AI 模型配置表
-- 支持多供应商（Claude、DeepSeek、OpenAI、通义千问等）
CREATE TABLE IF NOT EXISTS ai_model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID，0表示系统默认',
    provider VARCHAR(50) NOT NULL COMMENT '供应商：CLAUDE, DEEPSEEK, OPENAI, QWEN 等',
    provider_name VARCHAR(100) COMMENT '显示名称',
    api_url VARCHAR(500) NOT NULL COMMENT 'API 地址',
    api_key VARCHAR(500) NOT NULL COMMENT 'API Key（加密存储）',
    model VARCHAR(100) NOT NULL COMMENT '默认模型',
    available_models TEXT COMMENT '可用模型列表（JSON数组）',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认（每租户唯一）',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_tenant_default (tenant_id, is_default, deleted),
    INDEX idx_tenant (tenant_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';
