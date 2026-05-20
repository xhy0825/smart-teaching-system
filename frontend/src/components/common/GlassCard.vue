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
