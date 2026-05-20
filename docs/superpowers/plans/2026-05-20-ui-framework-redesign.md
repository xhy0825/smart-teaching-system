# UI整体框架及风格优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 全面升级UI框架，实现Glassmorphism风格、SaaS-Blue配色、深色模式支持的设计系统

**Architecture:** 基于Element Plus深度定制，建立完整的设计系统（CSS Variables + 主题切换），重构Layout组件，创建通用组件库，优化所有页面样式

**Tech Stack:** Vue 3, Element Plus, CSS Variables, Pinia, Google Fonts (Inter + Poppins)

---

## 文件结构映射

### 新建文件
- `frontend/src/assets/theme/light.css` - 浅色主题变量
- `frontend/src/assets/theme/dark.css` - 深色主题变量
- `frontend/src/styles/design-tokens.css` - 设计token（配色、字体、间距、圆角）
- `frontend/src/styles/glassmorphism.css` - Glassmorphism效果类
- `frontend/src/styles/utilities.css` - 工具类
- `frontend/src/components/common/GlassCard.vue` - 毛玻璃卡片组件
- `frontend/src/components/common/StatCard.vue` - 统计卡片组件

### 修改文件
- `frontend/src/assets/main.css` - 全局样式重构
- `frontend/src/main.ts` - 引入字体和新样式
- `frontend/src/App.vue` - 添加主题切换逻辑
- `frontend/src/store/app.ts` - 添加主题状态管理
- `frontend/src/views/Layout.vue` - 重构侧边栏和顶栏
- `frontend/src/views/Dashboard.vue` - 应用新设计系统
- `frontend/src/views/Login.vue` - 优化登录页样式

---

### Task 1: 设计系统基础 - CSS Variables和字体

**Files:**
- Create: `frontend/src/styles/design-tokens.css`
- Modify: `frontend/src/main.ts:1-20`
- Modify: `frontend/src/assets/main.css:1-19`

- [ ] **Step 1: 创建设计token文件**

创建 `frontend/src/styles/design-tokens.css`：

```css
/* 设计系统 - Token定义 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Poppins:wght@400;500;600;700&display=swap');

:root {
  /* === 字体系统 === */
  --font-heading: 'Poppins', sans-serif;
  --font-body: 'Inter', sans-serif;

  --text-xs: 12px;
  --text-sm: 13px;
  --text-base: 14px;
  --text-lg: 16px;
  --text-xl: 18px;
  --text-2xl: 24px;
  --text-3xl: 30px;

  --font-light: 300;
  --font-normal: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  /* === 间距系统（4px基准） === */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;

  /* === 圆角系统 === */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-full: 9999px;

  /* === 动画时长 === */
  --duration-fast: 150ms;
  --duration-normal: 300ms;
  --duration-slow: 500ms;
  --ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 浅色主题（默认） */
:root {
  /* 主色调 - SaaS Blue */
  --color-primary: #1E40AF;
  --color-primary-light: #3B82F6;
  --color-primary-lighter: #60A5FA;
  --color-primary-bg: #EFF6FF;

  /* 辅助色 */
  --color-success: #059669;
  --color-warning: #D97706;
  --color-danger: #DC2626;
  --color-info: #6366F1;

  /* 中性色 */
  --color-text-primary: #1E293B;
  --color-text-secondary: #475569;
  --color-text-muted: #94A3B8;
  --color-border: #E2E8F0;
  --color-bg-primary: #F8FAFC;
  --color-bg-secondary: #FFFFFF;
  --color-bg-hover: #F1F5F9;

  /* Element Plus 变量覆盖 */
  --el-color-primary: var(--color-primary);
  --el-color-success: var(--color-success);
  --el-color-warning: var(--color-warning);
  --el-color-danger: var(--color-danger);
  --el-color-info: var(--color-info);
  --el-bg-color: var(--color-bg-secondary);
  --el-text-color-primary: var(--color-text-primary);
  --el-text-color-regular: var(--color-text-secondary);
  --el-border-color: var(--color-border);
}
```

- [ ] **Step 2: 创建深色主题文件**

创建 `frontend/src/assets/theme/dark.css`：

```css
/* 深色主题 */
[data-theme="dark"] {
  --color-primary: #3B82F6;
  --color-primary-light: #60A5FA;
  --color-primary-lighter: #93C5FD;
  --color-primary-bg: #1E293B;

  --color-success: #34D399;
  --color-warning: #FBBF24;
  --color-danger: #F87171;
  --color-info: #818CF8;

  --color-text-primary: #F1F5F9;
  --color-text-secondary: #94A3B8;
  --color-text-muted: #64748B;
  --color-border: #334155;
  --color-bg-primary: #0F172A;
  --color-bg-secondary: #1E293B;
  --color-bg-hover: #334155;

  /* Element Plus 深色变量 */
  --el-color-primary: var(--color-primary);
  --el-bg-color: var(--color-bg-secondary);
  --el-text-color-primary: var(--color-text-primary);
  --el-text-color-regular: var(--color-text-secondary);
  --el-border-color: var(--color-border);
  --el-fill-color-blank: var(--color-bg-secondary);
  --el-fill-color: var(--color-bg-hover);
}
```

- [ ] **Step 3: 修改main.ts引入新样式**

修改 `frontend/src/main.ts`：

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import './assets/main.css'
import './styles/design-tokens.css'  // 新增：设计token
import './styles/glassmorphism.css'   // 新增：Glassmorphism效果
import './styles/utilities.css'       // 新增：工具类

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

- [ ] **Step 4: 重构全局样式**

修改 `frontend/src/assets/main.css`：

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: var(--font-body);
  font-size: var(--text-base);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  transition: background-color var(--duration-normal) var(--ease-in-out),
              color var(--duration-normal) var(--ease-in-out);
}

/* Element Plus 全局覆盖 */
.el-menu {
  border-right: none !important;
}

.el-table .el-table__header-wrapper .el-table__header th {
  background-color: var(--color-bg-hover) !important;
  font-family: var(--font-heading);
  font-weight: var(--font-semibold);
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all var(--duration-normal) var(--ease-in-out);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
```

- [ ] **Step 5: 验证样式引入**

运行：`cd frontend && npm run dev`
预期：开发服务器启动成功，无CSS错误

- [ ] **Step 6: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/styles/design-tokens.css frontend/src/assets/theme/dark.css frontend/src/assets/main.css frontend/src/main.ts
git commit -m "feat: 建立设计系统基础 - CSS Variables和字体引入"
```

---

### Task 2: Glassmorphism效果和工具类

**Files:**
- Create: `frontend/src/styles/glassmorphism.css`
- Create: `frontend/src/styles/utilities.css`

- [ ] **Step 1: 创建Glassmorphism效果类**

创建 `frontend/src/styles/glassmorphism.css`：

```css
/* Glassmorphism 毛玻璃效果 */

.glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all var(--duration-normal) var(--ease-in-out);
}

.glass:hover {
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.1);
}

/* 深色模式适配 */
[data-theme="dark"] .glass {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .glass:hover {
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.3);
}

/* 玻璃卡片变体 */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all var(--duration-normal) var(--ease-in-out);
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(30, 64, 175, 0.15);
}

[data-theme="dark"] .glass-card {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .glass-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

/* 玻璃按钮 */
.glass-btn {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}
```

- [ ] **Step 2: 创建工具类**

创建 `frontend/src/styles/utilities.css`：

```css
/* 工具类 */

/* 间距 */
.m-0 { margin: 0; }
.m-1 { margin: var(--space-1); }
.m-2 { margin: var(--space-2); }
.m-3 { margin: var(--space-3); }
.m-4 { margin: var(--space-4); }

.mt-0 { margin-top: 0; }
.mt-2 { margin-top: var(--space-2); }
.mt-4 { margin-top: var(--space-4); }
.mt-6 { margin-top: var(--space-6); }

.mb-0 { margin-bottom: 0; }
.mb-2 { margin-bottom: var(--space-2); }
.mb-4 { margin-bottom: var(--space-4); }

.p-0 { padding: 0; }
.p-2 { padding: var(--space-2); }
.p-4 { padding: var(--space-4); }
.p-6 { padding: var(--space-6); }

/* Flexbox */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.gap-2 { gap: var(--space-2); }
.gap-4 { gap: var(--space-4); }

/* 文字 */
.text-xs { font-size: var(--text-xs); }
.text-sm { font-size: var(--text-sm); }
.text-base { font-size: var(--text-base); }
.text-lg { font-size: var(--text-lg); }
.text-xl { font-size: var(--text-xl); }

.font-medium { font-weight: var(--font-medium); }
.font-semibold { font-weight: var(--font-semibold); }
.font-bold { font-weight: var(--font-bold); }

.text-primary { color: var(--color-text-primary); }
.text-secondary { color: var(--color-text-secondary); }
.text-muted { color: var(--color-text-muted); }

/* 圆角 */
.rounded-md { border-radius: var(--radius-md); }
.rounded-lg { border-radius: var(--radius-lg); }
.rounded-xl { border-radius: var(--radius-xl); }

/* 过渡 */
.transition {
  transition: all var(--duration-normal) var(--ease-in-out);
}

/* 阴影 */
.shadow-sm {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.shadow-md {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.shadow-lg {
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/styles/glassmorphism.css frontend/src/styles/utilities.css
git commit -m "feat: 添加Glassmorphism效果和工具类"
```

---

### Task 3: 主题切换机制

**Files:**
- Modify: `frontend/src/store/app.ts` (新建或修改)
- Modify: `frontend/src/App.vue:1-9`

- [ ] **Step 1: 创建或更新app store添加主题管理**

检查 `frontend/src/store/app.ts` 是否存在，如不存在则创建：

```typescript
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref<string>(localStorage.getItem('theme') || 'light')

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setTheme = (newTheme: 'light' | 'dark' | 'auto') => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    applyTheme()
  }

  const applyTheme = () => {
    const root = document.documentElement
    if (theme.value === 'auto') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
    } else {
      root.setAttribute('data-theme', theme.value)
    }
  }

  // 初始化主题
  applyTheme()

  // 监听系统主题变化
  if (typeof window !== 'undefined') {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (theme.value === 'auto') {
        applyTheme()
      }
    })
  }

  return {
    sidebarCollapsed,
    theme,
    toggleSidebar,
    setTheme,
    applyTheme
  }
})
```

- [ ] **Step 2: 修改App.vue添加主题切换支持**

修改 `frontend/src/App.vue`：

```vue
<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
// 确保主题在应用启动时应用
appStore.applyTheme()
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
```

- [ ] **Step 3: 验证主题切换**

在浏览器控制台测试：
```javascript
localStorage.setItem('theme', 'dark')
location.reload()
```
预期：页面切换为深色模式

- [ ] **Step 4: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/store/app.ts frontend/src/App.vue
git commit -m "feat: 实现主题切换机制 - 支持浅色/深色/自动模式"
```

---

### Task 4: 通用组件 - GlassCard和StatCard

**Files:**
- Create: `frontend/src/components/common/GlassCard.vue`
- Create: `frontend/src/components/common/StatCard.vue`

- [ ] **Step 1: 创建GlassCard组件**

创建 `frontend/src/components/common/GlassCard.vue`：

```vue
<template>
  <div class="glass-card">
    <div v-if="title || $slots.header" class="card-header">
      <h3 v-if="title" class="card-title">{{ title }}</h3>
      <slot name="header"></slot>
      <div v-if="$slots['header-right']" class="header-right">
        <slot name="header-right"></slot>
      </div>
    </div>
    <div class="card-body" :class="{ 'no-padding': noPadding }">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  noPadding?: boolean
}>()
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all var(--duration-normal) var(--ease-in-out);
  overflow: hidden;
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(30, 64, 175, 0.15);
}

[data-theme="dark"] .glass-card {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .glass-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-4);
}

.card-title {
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-text-primary);
  margin: 0;
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.card-body {
  padding: var(--space-6);
}

.card-body.no-padding {
  padding: 0;
}

.card-footer {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-hover);
}
</style>
```

- [ ] **Step 2: 创建StatCard组件**

创建 `frontend/src/components/common/StatCard.vue`：

```vue
<template>
  <div class="stat-card glass-card" @click="handleClick">
    <div class="stat-icon-wrapper" :style="{ background: gradient }">
      <el-icon :size="28"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-info">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
      <div v-if="trend" class="stat-trend" :class="{ up: trendUp, down: !trendUp }">
        <el-icon v-if="trendUp"><Top /></el-icon>
        <el-icon v-else><Bottom /></el-icon>
        <span>{{ trend }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Top, Bottom } from '@element-plus/icons-vue'

defineProps<{
  icon: string
  label: string
  value: number | string
  gradient: string
  trend?: string
  trendUp?: boolean
}>()

const emit = defineEmits<{
  (e: 'click'): void
}>()

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  cursor: pointer;
  padding: var(--space-5) !important;
}

.stat-card:hover {
  transform: translateY(-4px) !important;
  box-shadow: 0 12px 24px rgba(30, 64, 175, 0.15) !important;
}

[data-theme="dark"] .stat-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3) !important;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text-primary);
  line-height: 1.2;
  font-family: var(--font-heading);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  margin-top: var(--space-1);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-2);
  font-size: var(--text-xs);
}

.stat-trend.up {
  color: var(--color-success);
}

.stat-trend.down {
  color: var(--color-danger);
}
</style>
```

- [ ] **Step 3: 验证组件**

在Dashboard.vue中测试引入：
```vue
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
```
预期：组件正常导入，无错误

- [ ] **Step 4: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/components/common/GlassCard.vue frontend/src/components/common/StatCard.vue
git commit -m "feat: 添加通用组件 GlassCard和StatCard"
```

---

### Task 5: 重构Layout组件

**Files:**
- Modify: `frontend/src/views/Layout.vue` (大幅重构)

- [ ] **Step 1: 备份当前Layout.vue**

```bash
cd D:/JavaWork/edu
cp frontend/src/views/Layout.vue frontend/src/views/Layout.vue.backup
```

- [ ] **Step 2: 重构Layout.vue**

修改 `frontend/src/views/Layout.vue` 为全新设计：

```vue
<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarCollapsed ? '64px' : '240px'" class="sidebar glass-sidebar">
      <!-- Logo区域 -->
      <div class="logo-container">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32">
            <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!sidebarCollapsed" class="logo-text">智教云台</span>
        </transition>
      </div>

      <!-- 导航菜单 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          router
          class="sidebar-menu"
          :collapse-transition="false"
        >
          <el-menu-item index="/dashboard" class="menu-item">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页概览</template>
          </el-menu-item>

          <el-sub-menu index="question" class="sub-menu">
            <template #title>
              <el-icon><Collection /></el-icon>
              <span>题库管理</span>
            </template>
            <el-menu-item index="/question-bank" class="sub-menu-item">题库列表</el-menu-item>
            <el-menu-item index="/question" class="sub-menu-item">题目管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="exam" class="sub-menu">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>试卷管理</span>
            </template>
            <el-menu-item index="/exam-template" class="sub-menu-item">试卷模板</el-menu-item>
            <el-menu-item index="/exam-paper" class="sub-menu-item">试卷列表</el-menu-item>
            <el-menu-item index="/exam-generate" class="sub-menu-item">AI生成试卷</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="grading" class="sub-menu">
            <template #title>
              <el-icon><EditPen /></el-icon>
              <span>批改分析</span>
            </template>
            <el-menu-item index="/grading" class="sub-menu-item">试卷批改</el-menu-item>
            <el-menu-item index="/score-analysis" class="sub-menu-item">成绩分析</el-menu-item>
            <el-menu-item index="/wrong-questions" class="sub-menu-item">错题记录</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/student-profile" class="menu-item">
            <el-icon><UserFilled /></el-icon>
            <template #title>学生画像</template>
          </el-menu-item>

          <el-menu-item index="/class-profile" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>班级画像</template>
          </el-menu-item>

          <el-menu-item index="/ppt-maker" class="menu-item">
            <el-icon><Monitor /></el-icon>
            <template #title>PPT制作</template>
          </el-menu-item>

          <el-sub-menu index="system" class="sub-menu">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/user-manage" class="sub-menu-item">用户管理</el-menu-item>
            <el-menu-item index="/class-manage" class="sub-menu-item">班级管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>

      <!-- 底部折叠按钮 -->
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="toggleSidebar">
          <el-icon>
            <ArrowLeft v-if="!sidebarCollapsed" />
            <ArrowRight v-else />
          </el-icon>
          <transition name="fade">
            <span v-if="!sidebarCollapsed" class="collapse-text">收起菜单</span>
          </transition>
        </div>
      </div>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header glass-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 主题切换 -->
          <el-tooltip content="切换主题" placement="bottom">
            <el-dropdown trigger="click" @command="handleThemeChange">
              <el-button size="small" circle>
                <el-icon><Moon v-if="currentTheme === 'dark'" /><Sunny v-else-if="currentTheme === 'light'" /><Monitor v-else /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="light" :icon="Sunny">浅色模式</el-dropdown-item>
                  <el-dropdown-item command="dark" :icon="Moon">深色模式</el-dropdown-item>
                  <el-dropdown-item command="auto" :icon="Monitor">跟随系统</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>

          <!-- 快捷操作 -->
          <el-tooltip content="快速生成试卷" placement="bottom">
            <el-button type="primary" size="small" circle @click="router.push('/exam-generate')">
              <el-icon><MagicStick /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 用户信息 -->
          <el-dropdown class="user-dropdown" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ realName || '管理员' }}</span>
                <span class="user-role">教师</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item :icon="Setting">账号设置</el-dropdown-item>
                <el-dropdown-item divided :icon="SwitchButton" @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { User, Setting, SwitchButton, Moon, Sunny, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const currentTheme = computed(() => appStore.theme)
const realName = computed(() => userStore.realName)
const activeMenu = computed(() => route.path)

const currentRouteTitle = computed(() => {
  const routeMap: Record<string, string> = {
    '/question-bank': '题库列表',
    '/question': '题目管理',
    '/exam-template': '试卷模板',
    '/exam-paper': '试卷列表',
    '/exam-generate': 'AI生成试卷',
    '/grading': '试卷批改',
    '/score-analysis': '成绩分析',
    '/wrong-questions': '错题记录',
    '/student-profile': '学生画像',
    '/class-profile': '班级画像',
    '/ppt-maker': 'PPT制作',
    '/user-manage': '用户管理',
    '/class-manage': '班级管理'
  }
  return routeMap[route.path]
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleThemeChange = (theme: 'light' | 'dark' | 'auto') => {
  appStore.setTheme(theme)
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* 侧边栏 - Glassmorphism效果 */
.glass-sidebar {
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  transition: width var(--duration-normal) var(--ease-in-out);
  overflow: hidden;
}

[data-theme="dark"] .glass-sidebar {
  background: rgba(30, 41, 59, 0.8) !important;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

/* Logo区域 */
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 var(--space-4);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.logo-icon {
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  margin-left: var(--space-3);
  color: #fff;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  font-family: var(--font-heading);
  white-space: nowrap;
  overflow: hidden;
}

/* 菜单滚动区 */
.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  background: transparent;
  border: none;
  padding: var(--space-2) 0;
}

/* 菜单项样式 */
.menu-item,
.sub-menu {
  margin: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  color: var(--color-text-primary);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-in-out);
  font-weight: var(--font-medium);
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: var(--color-bg-hover);
  color: var(--color-primary);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
  font-weight: var(--font-semibold);
}

.sidebar-menu :deep(.el-menu-item.is-active:hover) {
  color: #fff;
}

.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent;
}

.sub-menu-item {
  height: 40px;
  line-height: 40px;
  margin: var(--space-1) 0;
  padding-left: 48px !important;
  color: var(--color-text-secondary);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
}

.sub-menu-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-primary);
}

.sub-menu-item.is-active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-weight: var(--font-medium);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: var(--space-3);
  border-top: 1px solid var(--color-border);
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  cursor: pointer;
  color: var(--color-text-muted);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.collapse-btn:hover {
  background: var(--color-bg-hover);
  color: var(--color-primary);
}

.collapse-text {
  margin-left: var(--space-2);
  font-size: var(--text-sm);
}

/* 顶部导航 - Glassmorphism效果 */
.glass-header {
  height: 56px;
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--space-6);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

[data-theme="dark"] .glass-header {
  background: rgba(30, 41, 59, 0.8) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

/* 用户下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-bg-hover);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.user-info:hover {
  background: var(--color-primary-bg);
}

.user-avatar {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-primary);
}

.user-role {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* 主内容区 */
.layout-main {
  background: var(--color-bg-primary);
  padding: var(--space-6);
  overflow-y: auto;
  transition: background-color var(--duration-normal) var(--ease-in-out);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--duration-fast) var(--ease-in-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 折叠状态调整 */
.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  padding-left: 0 !important;
  justify-content: center;
}

.sidebar-menu.el-menu--collapse :deep(.el-icon) {
  margin-right: 0;
}
</style>
```

- [ ] **Step 3: 删除备份文件**

```bash
rm frontend/src/views/Layout.vue.backup
```

- [ ] **Step 4: 验证Layout**

运行：`cd frontend && npm run dev`
打开浏览器访问 http://localhost:3000
预期：Layout显示新设计，侧边栏和顶栏应用Glassmorphism效果

- [ ] **Step 5: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/Layout.vue
git commit -m "feat: 重构Layout组件 - 应用Glassmorphism和全新设计"
```

---

### Task 6: 优化Dashboard页面

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`

- [ ] **Step 1: 备份Dashboard.vue**

```bash
cp frontend/src/views/Dashboard.vue frontend/src/views/Dashboard.vue.backup
```

- [ ] **Step 2: 重构Dashboard.vue应用新设计系统**

修改 `frontend/src/views/Dashboard.vue`，主要变更：
- 使用GlassCard组件替换内联glass-card样式
- 使用StatCard组件替换内联stat-card
- 应用设计系统的CSS Variables
- 优化响应式布局

由于Dashboard.vue内容较长（约500行），主要修改点：

1. **模板部分**：将`<div class="stat-card glass-card">`替换为`<StatCard>`
2. **样式部分**：移除硬编码颜色，使用CSS Variables

关键修改示例：

```vue
<!-- 统计卡片区 -->
<el-row :gutter="20" class="stat-row">
  <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="stat in statCards" :key="stat.label">
    <StatCard
      :icon="stat.icon"
      :label="stat.label"
      :value="stat.value"
      :gradient="stat.gradient"
      :trend="stat.trend"
      :trendUp="stat.trendUp"
      @click="stat.action && $router.push(stat.action)"
    />
  </el-col>
</el-row>

<!-- 图表区 -->
<el-row :gutter="20" class="chart-row">
  <el-col :xs="24" :sm="24" :md="12" :lg="12">
    <GlassCard title="学科分布" no-padding>
      <template #header-right>
        <el-tag size="small" type="info">实时</el-tag>
      </template>
      <div ref="subjectChartRef" class="chart-container"></div>
    </GlassCard>
  </el-col>
  ...
</el-row>
```

样式部分移除`.glass-card`和`.stat-card`定义（已在组件中），保留页面特定样式。

- [ ] **Step 3: 删除备份并验证**

```bash
rm frontend/src/views/Dashboard.vue.backup
```

运行：`cd frontend && npm run dev`
预期：Dashboard显示新设计，GlassCard和StatCard正常工作

- [ ] **Step 4: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/Dashboard.vue
git commit -m "feat: 优化Dashboard页面 - 应用新设计系统和组件"
```

---

### Task 7: 优化Login页面

**Files:**
- Modify: `frontend/src/views/Login.vue`

- [ ] **Step 1: 优化Login.vue样式**

修改 `frontend/src/views/Login.vue`：
- 确保与整体设计系统一致
- 保持现有科技风格，但使用CSS Variables
- 添加深色模式支持

主要修改：将硬编码颜色替换为CSS Variables：

```css
/* 修改前 */
background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 50%, #60A5FA 100%);

/* 修改后 */
background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 50%, var(--color-primary-lighter) 100%);
```

- [ ] **Step 2: 验证Login页面**

运行：`cd frontend && npm run dev`
访问 http://localhost:3000/login
预期：Login页面样式正常，支持深色模式

- [ ] **Step 3: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/Login.vue
git commit -m "feat: 优化Login页面 - 适配设计系统和深色模式"
```

---

### Task 8: 响应式测试和优化

**Files:**
- Modify: `frontend/src/views/Layout.vue` (响应式调整)
- Modify: `frontend/src/views/Dashboard.vue` (响应式调整)

- [ ] **Step 1: 测试375px断点（手机）**

在Chrome DevTools中设置设备为iPhone 12 Pro (375x812)
预期：
- 侧边栏默认收起
- Stat Cards 1列
- 图表区单列堆叠
- 文字和按钮大小合适

- [ ] **Step 2: 测试768px断点（平板）**

在Chrome DevTools中设置宽度为768px
预期：
- 侧边栏可收起
- Stat Cards 2列
- 图表区2列并排
- 布局合理

- [ ] **Step 3: 测试1440px断点（桌面）**

在Chrome DevTools中设置宽度为1440px
预期：
- 侧边栏展开
- Stat Cards 4列
- 图表区2列并排
- 布局完整

- [ ] **Step 4: 修复响应式问题**

根据测试结果，调整Layout.vue和Dashboard.vue的响应式样式

- [ ] **Step 5: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/views/Layout.vue frontend/src/views/Dashboard.vue
git commit -m "fix: 响应式布局优化 - 适配3个断点"
```

---

### Task 9: 深色模式测试和优化

**Files:**
- 无新文件，测试现有实现

- [ ] **Step 1: 测试浅色模式**

切换主题为浅色，检查所有页面
预期：所有组件显示正常，配色符合设计系统

- [ ] **Step 2: 测试深色模式**

切换主题为深色，检查所有页面
预期：
- 背景色正确（深蓝色系）
- 文字颜色正确（浅灰色系）
- Glassmorphism效果正确（深色半透明）
- Element Plus组件适配正确

- [ ] **Step 3: 测试自动模式**

切换主题为自动，修改系统主题
预期：主题跟随系统切换

- [ ] **Step 4: 修复深色模式问题**

根据测试结果，调整深色主题CSS

- [ ] **Step 5: Commit**

```bash
cd D:/JavaWork/edu
git add frontend/src/assets/theme/dark.css frontend/src/styles/glassmorphism.css
git commit -m "fix: 深色模式优化 - 修复组件适配问题"
```

---

### Task 10: 性能优化和最终审查

**Files:**
- 检查所有修改的文件

- [ ] **Step 1: 运行Lighthouse测试**

在Chrome中打开 http://localhost:3000
运行Lighthouse测试
预期：
- Performance ≥ 90
- Accessibility ≥ 90
- Best Practices ≥ 90
- SEO ≥ 80

- [ ] **Step 2: 检查可访问性**

使用axe DevTools Chrome扩展
预期：0个critical/serious违规

- [ ] **Step 3: 优化性能**

根据Lighthouse报告优化：
- 压缩图片
- 减少不必要的重绘
- 优化CSS选择器

- [ ] **Step 4: 最终审查**

检查所有页面：
- [ ] 配色统一（SaaS-Blue）
- [ ] 字体统一（Inter + Poppins）
- [ ] Glassmorphism效果一致
- [ ] 深色模式完整支持
- [ ] 响应式布局正常
- [ ] 动画流畅（150-300ms）

- [ ] **Step 5: Final Commit**

```bash
cd D:/JavaWork/edu
git add -A
git commit -m "feat: UI整体框架及风格优化完成 - 全面升级到Glassmorphism风格"
```

---

## 自审检查

**1. Spec覆盖：**
- [x] 架构设计 → Task 1-5 (设计系统 + Layout重构)
- [x] 设计系统 → Task 1-2 (CSS Variables + Glassmorphism)
- [x] 关键组件 → Task 4 (GlassCard + StatCard)
- [x] 深色模式 → Task 3, 9 (主题切换 + 测试)
- [x] 响应式设计 → Task 8 (3个断点测试)
- [x] 动画系统 → Task 1 (CSS Variables定义)

**2. Placeholder扫描：** 无TBD/TODO/占位符

**3. 类型一致性：** 所有组件props和事件名称一致

**计划完成。**
