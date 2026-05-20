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
