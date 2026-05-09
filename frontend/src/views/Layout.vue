<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarCollapsed ? '64px' : '240px'" class="sidebar">
      <!-- Logo区域 -->
      <div class="logo-container">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32">
            <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!sidebarCollapsed" class="logo-text">教师智能教学系统</span>
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
          <!-- 首页 -->
          <el-menu-item index="/dashboard" class="menu-item">
            <el-icon class="menu-icon"><HomeFilled /></el-icon>
            <template #title>
              <span class="menu-text">首页概览</span>
            </template>
          </el-menu-item>

          <!-- 题库管理 -->
          <el-sub-menu index="question" class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><Collection /></el-icon>
              <span class="menu-text">题库管理</span>
            </template>
            <el-menu-item index="/question-bank" class="sub-menu-item">
              <el-icon><FolderOpened /></el-icon>
              <span>题库列表</span>
            </el-menu-item>
            <el-menu-item index="/question" class="sub-menu-item">
              <el-icon><Document /></el-icon>
              <span>题目管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 试卷管理 -->
          <el-sub-menu index="exam" class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><Document /></el-icon>
              <span class="menu-text">试卷管理</span>
            </template>
            <el-menu-item index="/exam-template" class="sub-menu-item">
              <el-icon><Files /></el-icon>
              <span>试卷模板</span>
            </el-menu-item>
            <el-menu-item index="/exam-paper" class="sub-menu-item">
              <el-icon><Tickets /></el-icon>
              <span>试卷列表</span>
            </el-menu-item>
            <el-menu-item index="/exam-generate" class="sub-menu-item">
              <el-icon><MagicStick /></el-icon>
              <span>AI生成试卷</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 批改分析 -->
          <el-sub-menu index="grading" class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><EditPen /></el-icon>
              <span class="menu-text">批改分析</span>
            </template>
            <el-menu-item index="/grading" class="sub-menu-item">
              <el-icon><Finished /></el-icon>
              <span>试卷批改</span>
            </el-menu-item>
            <el-menu-item index="/score-analysis" class="sub-menu-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>成绩分析</span>
            </el-menu-item>
            <el-menu-item index="/wrong-questions" class="sub-menu-item">
              <el-icon><Warning /></el-icon>
              <span>错题记录</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 学生画像 -->
          <el-menu-item index="/student-profile" class="menu-item">
            <el-icon class="menu-icon"><UserFilled /></el-icon>
            <template #title>
              <span class="menu-text">学生画像</span>
            </template>
          </el-menu-item>

          <!-- PPT制作 -->
          <el-menu-item index="/ppt-maker" class="menu-item">
            <el-icon class="menu-icon"><Monitor /></el-icon>
            <template #title>
              <span class="menu-text">PPT制作</span>
            </template>
          </el-menu-item>

          <!-- 系统设置 -->
          <el-sub-menu index="system" class="sub-menu">
            <template #title>
              <el-icon class="menu-icon"><Setting /></el-icon>
              <span class="menu-text">系统设置</span>
            </template>
            <el-menu-item index="/user-manage" class="sub-menu-item">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/class-manage" class="sub-menu-item">
              <el-icon><School /></el-icon>
              <span>班级管理</span>
            </el-menu-item>
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
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
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
import { User, Setting, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const realName = computed(() => userStore.realName)
const activeMenu = computed(() => route.path)

// 获取当前路由标题
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
    '/ppt-maker': 'PPT制作',
    '/user-manage': '用户管理',
    '/class-manage': '班级管理'
  }
  return routeMap[route.path]
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
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

/* ========== 侧边栏样式 ========== */
.sidebar {
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  border-right: 1px solid #e4e7ed;
}

/* Logo区域 */
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.logo-icon {
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  margin-left: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
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
  padding: 8px 0;
}

/* 菜单项样式 */
.menu-item,
.sub-menu {
  margin: 4px 8px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
  color: #303133;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background: #e8f4ff;
  color: #409eff;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  font-weight: 600;
}

.menu-icon {
  font-size: 18px;
  margin-right: 8px;
  color: #606266;
}

.sidebar-menu .el-menu-item.is-active .menu-icon {
  color: #fff;
}

.sidebar-menu .el-menu-item:hover .menu-icon,
.sidebar-menu .el-sub-menu__title:hover .menu-icon {
  color: #409eff;
}

.menu-text {
  font-size: 14px;
}

/* 子菜单项 */
.sidebar-menu .el-sub-menu .el-menu {
  background: transparent;
}

.sub-menu-item {
  height: 40px;
  line-height: 40px;
  margin: 2px 0;
  padding-left: 48px !important;
  color: #606266;
  border-radius: 6px;
  font-size: 13px;
}

.sub-menu-item:hover {
  background: #e8f4ff;
  color: #409eff;
}

.sub-menu-item.is-active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  cursor: pointer;
  color: #909399;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: #e8f4ff;
  color: #409eff;
}

.collapse-text {
  margin-left: 8px;
  font-size: 13px;
}

/* ========== 顶部导航样式 ========== */
.layout-header {
  height: 56px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 用户下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px;
  border-radius: 24px;
  background: #f5f7fa;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #e8f4ff;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

/* ========== 主内容区 ========== */
.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

/* ========== 折叠状态调整 ========== */
.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.sidebar-menu.el-menu--collapse .el-menu-item,
.sidebar-menu.el-menu--collapse .el-sub-menu__title {
  padding-left: 0 !important;
  justify-content: center;
}

.sidebar-menu.el-menu--collapse .menu-icon {
  margin-right: 0;
}
</style>