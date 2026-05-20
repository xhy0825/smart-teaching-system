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