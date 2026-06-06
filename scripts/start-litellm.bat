@echo off
REM LiteLLM Proxy Server 启动脚本（Windows）

REM 设置环境变量（请替换为真实的 API Key）
set ANTHROPIC_API_KEY=%ANTHROPIC_API_KEY%
set DEEPSEEK_API_KEY=%DEEPSEEK_API_KEY%
set OPENAI_API_KEY=%OPENAI_API_KEY%
set QWEN_API_KEY=%QWEN_API_KEY%

REM 如果环境变量未设置，提示用户
if "%ANTHROPIC_API_KEY%"=="" (
    echo 警告：ANTHROPIC_API_KEY 未设置
)

REM 配置文件路径
set CONFIG_FILE=%~dp0\..\litellm-config.yaml

REM 检查配置文件是否存在
if not exist "%CONFIG_FILE%" (
    echo 错误：配置文件 %CONFIG_FILE% 不存在
    pause
    exit /b 1
)

REM 启动 LiteLLM Proxy Server
echo 正在启动 LiteLLM Proxy Server...
echo 配置文件：%CONFIG_FILE%
echo 监听端口：8000
echo API 文档：http://localhost:8000/docs
echo.

litellm --config "%CONFIG_FILE%" --port 8000 --verbose

REM 如果启动失败，保持窗口打开
if errorlevel 1 (
    echo.
    echo LiteLLM Proxy Server 启动失败
    pause
)
