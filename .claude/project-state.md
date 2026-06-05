# SDLC 项目状态（活文档 — 持续更新）

> **COMPACTION 保护区域。** 唯一状态存储，通过 CLAUDE.md @import 加载。升级时不被覆盖。

```yaml
# 项目级（跨任务持久化）
project_roadmap: "新增智能助手（AI Tutor）+ 大模型配置页面"  # ≤50字
completed_tasks:
  - task: "优化首页Dashboard现代数据大屏风格"
    prd_summary: "R1:大屏风格 R2:统计卡片 R3:ECharts R4:快捷操作 R5:列表增强 R6:响应式 R7:加载状态"
    key_decisions: ["Data-Dense Dashboard风格", "蓝色系#1E40AF", "ECharts图表"]
    files_count: 3
    completed_at: "2026-05-20"
  - task: "UI整体框架及风格优化"
    prd_summary: "R1:布局框架 R2:设计系统 R3:Glassmorphism R4:深色模式 R5:通用组件 R6:页面优化 R7:响应式 R8:字体系统"
    key_decisions: ["Glassmorphism风格", "SaaS-Blue配色", "Inter+Poppins字体", "深度定制Element Plus"]
    files_count: 18
    completed_at: "2026-05-20"
  - task: "修复菜单导航栏收起显示异常"
    prd_summary: "R1:图标居中 R2:文字隐藏 R3:箭头隐藏 R4:动画流畅"
    key_decisions: ["Element Plus折叠API", "CSS深度选择器"]
    files_count: 1
    completed_at: "2026-05-20"
  - task: "实现极简出卷+拍照批改（MVP+2扩展）"
    prd_summary: "R1:极简出卷 R2:拍照批改 R3:API提取 R4:Redis管理 R5:成本监控 R6:边界测试"
    key_decisions: ["ClaudeAPIClient公共方法", "新建VisionAIService", "Redis对话管理", "成本监控", "边界测试完成", "并发测试完成"]
    files_count: 9
    completed_at: "2026-06-05"
  - task: "新增智能助手+大模型配置页面"
    prd_summary: "R1:AI助教对话 R2:大模型配置 R3:TutorController R4:前端Tutor+ModelConfig R5:路由已添加"
    key_decisions: ["新增TutorController", "前端Tutor.vue", "ModelConfig.vue配置页面", "前端路由已添加"]
    files_count: 4
    completed_at: "2026-06-05"
global_architecture: ["Spring Boot 3.2 + Vue 3 + Element Plus + Claude API", "ClaudeAPIClient公共方法", "VisionAIService拍照批改", "Redis对话管理", "成本监控", "智能助手（新增）", "大模型配置页面（新增）"]  # ≤5条#

# 当前任务（重置时归档后清空）
current_phase: P3  # P0-P5
task_description: "测试验证多供应商大模型配置"  # ≤30字
started_at: "2026-06-05"
last_updated: "2026-06-06"
prd_file: ""  # PRD 路径
architecture_decisions: ["AIClient接口统一调用", "OpenAICompatibleClient支持DeepSeek", "数据库驱动配置", "AIClientFactory动态创建"]  # ≤5条#
modified_files:
  - "backend/src/main/resources/db/migration/add_ai_model_config.sql"
  - "backend/src/main/java/com/edu/ai/entity/AIModelConfig.java"
  - "backend/src/main/java/com/edu/ai/mapper/AIModelConfigMapper.java"
  - "backend/src/main/resources/mapper/AIModelConfigMapper.xml"
  - "backend/src/main/java/com/edu/ai/service/AIModelConfigService.java"
  - "backend/src/main/java/com/edu/ai/controller/AIModelConfigController.java"
  - "backend/src/main/java/com/edu/ai/client/AIClient.java"
  - "backend/src/main/java/com/edu/ai/client/ClaudeClient.java"
  - "backend/src/main/java/com/edu/ai/client/OpenAICompatibleClient.java"
  - "backend/src/main/java/com/edu/ai/client/AIClientFactory.java"
  - "backend/src/main/java/com/edu/ai/provider/CloudAIProvider.java"
  - "backend/src/main/java/com/edu/ai/provider/AIProviderFactory.java"
  - "backend/src/main/java/com/edu/ai/provider/PrivateAIProvider.java"
  - "backend/src/main/java/com/edu/ai/provider/AIProvider.java"
  - "backend/src/main/java/com/edu/ai/controller/TutorController.java"
  - "frontend/src/api/ai.ts"
  - "frontend/src/views/ModelConfig.vue"
todo_items: []
review_retry_count: 0

phase_history:
  - "P1→P2: 2026-05-20 PRD确认"
  - "P2→P3: 2026-05-20 编码完成"
  - "P3→P4: 2026-05-20 测试通过"
  - "P4→P5: 2026-05-20 审查通过"
  - "P5→P0: 2026-05-20 交付完成"
  - "P0→P2: 2026-06-05 plan-eng-review完成，开始实现"
  - "P2→P3: 2026-06-05 编码完成，测试通过"
  - "P3→P4: 2026-06-05 测试验证通过"
  - "P4→P5: 2026-06-05 审查通过（需修复阻断问题）"
  - "P5→P0: 2026-06-05 交付完成"
  - "P0→P1: 2026-06-05 新增智能助手+大模型配置页面"
  - "P1→P2: 2026-06-05 开始实现智能助手+配置页面"
  - "P2→P3: 2026-06-05 编码完成，测试通过"
  - "P3→P4: 2026-06-05 测试验证通过，进入P4审查"
  - "P4→P5: 2026-06-05 审查完成，进入部署交付"
  - "P5→P0: 2026-06-05 交付完成，新任务开始"
  - "P0→P2: 2026-06-05 多供应商大模型配置（Claude+DeepSeek+OpenAI兼容）"
  - "P2→P3: 2026-06-05 编码完成，开始测试验证"
  - "P3: 2026-06-06 编译修复（CostMonitoringService+VisionAIService），推送182f41c"
key_context: "P3测试：多供应商配置编译修复+AI助教页面重新设计（8/10）"  # ≤50字
```

**更新时机**：新任务→归档+重置 | PRD确认→写 prd.md | 阶段推进→更新 phase | 文件修改→记路径 | 架构→记决策 | 压缩前→更新全部**

**Compact 保留**：current_phase、task_description、prd_file、modified_files、key_context、project_roadmap、completed_tasks（最近3个）、global_architecture、prd.md文件**

**Compact 删除**：phase_history详细、todo_items、多余completed_tasks、requirements_clarification**

详见 `.claude/rules/09-memory-management.md`
