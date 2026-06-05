<template>
  <div class="model-config">
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>大模型配置</span>
          <el-tag type="info">Claude API</el-tag>
        </div>
      </template>

      <!-- API Key 配置 -->
      <el-form label-width="120px">
        <el-form-item label="API Key">
          <el-input
            v-model="apiKey"
            type="password"
            placeholder="请输入 Claude API Key"
            show-password
            class="config-input"
          />
          <el-button
            type="primary"
            :loading="testing"
            @click="testConnection"
          >
            <el-icon><Connection /></el-icon>
            测试连接
          </el-button>
        </el-form-item>

        <el-form-item label="当前模型">
          <el-select v-model="currentModel" class="model-select">
            <el-option label="Claude Sonnet 4.6" value="claude-sonnet-4-6" />
            <el-option label="Claude Haiku 3.5" value="claude-haiku-3.5" />
            <el-option label="Claude Opus 3" value="claude-opus-3" />
          </el-select>
        </el-form-item>

        <el-form-item label="每次最大 Token">
          <el-slider
            v-model="maxTokens"
            :min="100"
            :max="4000"
            :step="100"
            show-input
          />
        </el-form-item>

        <el-form-item label="每日成本限额">
          <el-input-number
            v-model="dailyLimit"
            :min="10"
            :max="500"
            :step="10"
          />
          <span class="unit">美元/天</span>
        </el-form-item>
      </el-form>

      <!-- 使用统计 -->
      <el-divider>使用统计</el-divider>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ stats.callCount }}</div>
          <div class="stat-label">调用次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.tokenCount | formatNumber }}</div>
          <div class="stat-label">Token 消耗</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">${{ stats.dailyCost | currency }}</div>
          <div class="stat-label">今日成本</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.monthlyCost | currency }}</div>
          <div class="stat-label">本月成本</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" @click="saveConfig" :loading="saving">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
        <el-button @click="resetConfig">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, Check, RefreshRight } from '@element-plus/icons-vue'
import axios from 'axios'

const apiKey = ref('')
const currentModel = ref('claude-sonnet-4-6')
const maxTokens = ref(2000)
const dailyLimit = ref(50)
const testing = ref(false)
const saving = ref(false)

const stats = ref({
  callCount: 0,
  tokenCount: 0,
  dailyCost: 0.0,
  monthlyCost: 0.0
})

// 测试连接
const testConnection = async () => {
  if (!apiKey.value) {
    ElMessage.warning('请先输入 API Key')
    return
  }

  testing.value = true
  try {
    // TODO: 调用后端测试接口
    // const res = await axios.post('/api/ai/test-connection', { apiKey: apiKey.value })
    setTimeout(() => {
      ElMessage.success('连接成功！模型可用。')
      testing.value = false
    }, 1500)
  } catch (error) {
    ElMessage.error('连接失败：' + error.message)
    testing.value = false
  }
}

// 保存配置
const saveConfig = async () => {
  saving.value = true
  try {
    // TODO: 调用后端保存接口
    // await axios.post('/api/ai/save-config', {
    //   apiKey: apiKey.value,
    //   model: currentModel.value,
    //   maxTokens: maxTokens.value,
    //   dailyLimit: dailyLimit.value
    // })
    setTimeout(() => {
      ElMessage.success('配置保存成功！')
      saving.value = false
    }, 1000)
  } catch (error) {
    ElMessage.error('保存失败：' + error.message)
    saving.value = false
  }
}

// 重置配置
const resetConfig = () => {
  apiKey.value = ''
  currentModel.value = 'claude-sonnet-4-6'
  maxTokens.value = 2000
  dailyLimit.value = 50
  ElMessage.info('已重置为默认配置')
}

// 格式化数字
const formatNumber = (val) => {
  return val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化货币
const currency = (val) => {
  return '$' + val.toFixed(2)
}
</script>

<style scoped>
.model-config {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
}

.glass-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
}

.config-input {
    width: 400px;
}

.model-select {
    width: 300px;
}

.unit {
    margin-left: 8px;
    color: var(--el-text-color-secondary);
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin: 20px 0;
}

.stat-card {
    background: var(--el-fill-color-light);
    border-radius: 8px;
    padding: 16px;
    text-align: center;
}

.stat-value {
    font-size: 24px;
    font-weight: bold;
    color: var(--el-color-primary);
}

.stat-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
}

.actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--el-border-color);
}
</style>
