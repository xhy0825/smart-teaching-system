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
            <el-icon :size="20" class="menu-icon"><Home /></el-icon>
            <template #title>首页概览</template>
          </el-menu-item>

          <el-sub-menu index="question" class="sub-menu" :class="{ 'is-active-parent': activeParentMenu === 'question' }">
            <template #title>
              <el-icon :size="20" class="menu-icon"><BookOpen /></el-icon>
              <span>题库管理</span>
            </template>
            <el-menu-item index="/question-bank" class="sub-menu-item">题库列表</el-menu-item>
            <el-menu-item index="/question" class="sub-menu-item">题目管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="exam" class="sub-menu" :class="{ 'is-active-parent': activeParentMenu === 'exam' }">
            <template #title>
              <el-icon :size="20" class="menu-icon"><FileText /></el-icon>
              <span>试卷管理</span>
            </template>
            <el-menu-item index="/exam-template" class="sub-menu-item">试卷模板</el-menu-item>
            <el-menu-item index="/exam-paper" class="sub-menu-item">试卷列表</el-menu-item>
            <el-menu-item index="/exam-generate" class="sub-menu-item">AI生成试卷</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="grading" class="sub-menu" :class="{ 'is-active-parent': activeParentMenu === 'grading' }">
            <template #title>
              <el-icon :size="20" class="menu-icon"><PenTool /></el-icon>
              <span>批改分析</span>
            </template>
            <el-menu-item index="/grading" class="sub-menu-item">试卷批改</el-menu-item>
            <el-menu-item index="/score-analysis" class="sub-menu-item">成绩分析</el-menu-item>
            <el-menu-item index="/wrong-questions" class="sub-menu-item">错题记录</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/student-profile" class="menu-item">
            <el-icon :size="20" class="menu-icon"><Users /></el-icon>
            <template #title>学生画像</template>
          </el-menu-item>

          <el-menu-item index="/class-profile" class="menu-item">
            <el-icon :size="20" class="menu-icon"><BarChart3 /></el-icon>
            <template #title>班级画像</template>
          </el-menu-item>

          <el-menu-item index="/ppt-maker" class="menu-item">
            <el-icon :size="20" class="menu-icon"><Presentation /></el-icon>
            <template #title>PPT制作</template>
          </el-menu-item>

          <el-sub-menu index="system" class="sub-menu" :class="{ 'is-active-parent': activeParentMenu === 'system' }">
            <template #title>
              <el-icon :size="20" class="menu-icon"><Setting /></el-icon>
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
          <el-icon :size="18">
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
import { Home, BookOpen, FileText, PenTool, Users, BarChart3, Presentation, ArrowLeft, ArrowRight } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const currentTheme = computed(() => appStore.theme)
const realName = computed(() => userStore.realName)
const activeMenu = computed(() => route.path)

// 计算当前激活的一级菜单（用于二级菜单选中时高亮一级菜单）
const activeParentMenu = computed(() => {
  const path = route.path
  const menuMap: Record<string, string> = {
    '/question-bank': 'question',
    '/question': 'question',
    '/exam-template': 'exam',
    '/exam-paper': 'exam',
    '/exam-generate': 'exam',
    '/grading': 'grading',
    '/score-analysis': 'grading',
    '/wrong-questions': 'grading',
    '/user-manage': 'system',
    '/class-manage': 'system'
  }
  return menuMap[path] || ''
})

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
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
  font-weight: var(--font-semibold);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.sidebar-menu :deep(.el-menu-item.is-active:hover) {
  color: #fff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
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
:deep(.el-menu--collapse) {
  width: 64px !important;
}

/* 隐藏折叠状态下的文字和箭头 */
:deep(.el-menu--collapse) .el-sub-menu__title > span:not(.el-icon),
:deep(.el-menu--collapse) .el-menu-item > span:not(.el-icon) {
  display: none !important;
}

/* 隐藏折叠状态下的箭头 */
:deep(.el-menu--collapse) .el-sub-menu__icon-arrow {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
}

/* 折叠状态下隐藏子菜单 */
:deep(.el-menu--collapse) .el-sub-menu .el-menu {
  display: none !important;
}

/* 折叠状态下激活菜单项样式 */
:deep(.el-menu--collapse) .el-menu-item.is-active {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%) !important;
  color: #fff !important;
  border-radius: var(--radius-md) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

:deep(.el-menu--collapse) .el-menu-item.is-active .el-icon {
  color: #fff !important;
}

/* 折叠状态下未激活菜单项悬停效果 */
:deep(.el-menu--collapse) .el-menu-item:not(.is-active):hover,
:deep(.el-menu--collapse) .el-sub-menu__title:not(.is-active):hover {
  background: var(--color-bg-hover) !important;
}

/* 折叠状态下一级菜单高亮（二级菜单选中时） */
:deep(.el-menu--collapse) .el-sub-menu.is-active-parent .el-sub-menu__title {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%) !important;
  color: #fff !important;
  border-radius: var(--radius-md) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3) !important;
}

:deep(.el-menu--collapse) .el-sub-menu.is-active-parent .el-sub-menu__title .el-icon {
  color: #fff !important;
}
</style>
