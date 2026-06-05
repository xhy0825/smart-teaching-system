<template>
  <div class="app-root">
    <router-view />
  </div>
</template>

<script setup lang="ts">
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
.app-root {
  width: 100%;
  height: 100%;
}
</style>
