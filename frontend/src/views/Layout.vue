<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'">
      <div class="logo">
        <span v-if="!sidebarCollapsed">教师智能教学系统</span>
        <span v-else>教</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="question">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>题库管理</span>
          </template>
          <el-menu-item index="/question-bank">题库列表</el-menu-item>
          <el-menu-item index="/question">题目管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="exam">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>试卷管理</span>
          </template>
          <el-menu-item index="/exam-template">试卷模板</el-menu-item>
          <el-menu-item index="/exam-paper">试卷列表</el-menu-item>
          <el-menu-item index="/exam-generate">试卷生成</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="grading">
          <template #title>
            <el-icon><EditPen /></el-icon>
            <span>批改分析</span>
          </template>
          <el-menu-item index="/grading">试卷批改</el-menu-item>
          <el-menu-item index="/score-analysis">成绩分析</el-menu-item>
          <el-menu-item index="/wrong-questions">错题记录</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleSidebar">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ realName }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const realName = computed(() => userStore.realName)
const activeMenu = computed(() => route.path)

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
  height: 100%;
}

.el-aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.sidebar-menu {
  background-color: #304156;
  color: #bfcbd9;
  height: calc(100% - 60px);
  border-right: none;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background-color: #263445;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #fff;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.collapse-icon {
  cursor: pointer;
  font-size: 20px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>