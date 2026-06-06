#!/bin/bash
# LiteLLM Proxy Server 启动脚本

# 设置环境变量（从 .env 文件或系统环境变量读取）
# 如果系统环境变量未设置，使用默认值（仅用于测试）

export ANTHROPIC_API_KEY="${ANTHROPIC_API_KEY:-your_anthropic_key_here}"
export DEEPSEEK_API_KEY="${DEEPSEEK_API_KEY:-your_deepseek_key_here}"
export OPENAI_API_KEY="${OPENAI_API_KEY:-your_openai_key_here}"
export QWEN_API_KEY="${QWEN_API_KEY:-your_qwen_key_here}"

# 配置文件路径
CONFIG_FILE="$(dirname "$0")/litellm-config.yaml"

# 检查配置文件是否存在
if [ ! -f "$CONFIG_FILE" ]; then
    echo "错误：配置文件 $CONFIG_FILE 不存在"
    exit 1
fi

# 检查 litellm 命令是否可用
if ! command -v litellm &> /dev/null; then
    echo "错误：litellm 命令未找到，请先安装：pip install litellm[proxy]"
    exit 1
fi

# 启动 LiteLLM Proxy Server
echo "正在启动 LiteLLM Proxy Server..."
echo "配置文件：$CONFIG_FILE"
echo "监听端口：8000"
echo "API 文档：http://localhost:8000/docs"
echo ""

litellm --config "$CONFIG_FILE" --port 8000 --verbose

# 如果启动失败，保持窗口打开
if [ $? -ne 0 ]; then
    echo ""
    echo "LiteLLM Proxy Server 启动失败"
    read -p "按 Enter 键退出..."
fi
