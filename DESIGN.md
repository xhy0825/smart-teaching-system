# Design System — 教师智能教学系统

## Product Context
- **What this is:** 教师智能教学系统（AI-powered teaching assistant）
- **Who it's for:** K-12 教师，需要快速生成试卷、批改作业、分析学生数据
- **Space/industry:** 教育科技（EdTech）
- **Project type:** Web 应用（Vue 3 前端 + Spring Boot 后端）

## Aesthetic Direction
- **Direction:** Editorial/Modern（杂志风+现代感）
- **Decoration level:** intentional（适度装饰）
- **Mood:** 专业、高效、可信赖
- **Reference sites:** 教育科技 SaaS 产品（如 ClassDojo、Nearpod）

## Typography
- **Display/Hero:** Satoshi（现代无衬线）
- **Body:** Instrument Sans（清晰易读）
- **UI/Labels:** same as body
- **Data/Tables:** IBM Plex Mono（数据对齐）
- **Code:** JetBrains Mono
- **Loading:** Google Fonts CDN
- **Scale:** 
  - xs: 12px
  - sm: 14px
  - md: 16px
  - lg: 18px
  - xl: 20px
  - 2xl: 24px

## Color
- **Approach:** balanced（主色+辅助色+语义色）
- **Primary:** #1E40AF（SaaS 蓝）
- **Secondary:** #7C3AED（浅紫蓝）
- **Neutrals:** 
  - Lightest: #F8FAFC
  - Light: #F1F5F9
  - Medium: #94A3B8
  - Dark: #1E293B
  - Darkest: #0F172A
- **Semantic:** 
  - success: #10B981
  - warning: #F59E0B
  - error: #EF4444
  - info: #3B82F6
- **Dark mode:** 
  - 背景变暗 20%
  - 降低饱和度 15%

## Spacing
- **Base unit:** 4px
- **Density:** comfortable（舒适密度）
- **Scale:** 
  - xs: 4px
  - sm: 8px
  - md: 16px
  - lg: 24px
  - xl: 32px
  - 2xl: 48px
  - 3xl: 64px

## Layout
- **Approach:** grid-disciplined（严格网格）
- **Grid:** 12 列网格
- **Max content width:** 1280px
- **Border radius:** 
  - sm: 4px
  - md: 8px
  - lg: 12px
  - full: 9999px

## Motion
- **Approach:** intentional（适度动画）
- **Easing:** ease-out（进入）/ ease-in（退出）
- **Duration:** 
  - micro: 50-100ms
  - short: 150-250ms
  - medium: 250-400ms
  - long: 400-700ms

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-05 | 初始设计系统创建 | 基于 /design-consultation 技能，为 AI 助教+大模型配置页面创建统一设计系统 |
| 2026-06-05 | 采用 Glassmorphism + SaaS-Blue | 符合教育科技产品调性，专业且现代 |
| 2026-06-05 | 使用 Inter + Poppins 字体 | 清晰易读，符合现代 Web 应用标准 |
| 2026-06-05 | 采用 Element Plus 组件库 | 统一 UI 风格，提高开发效率 |
