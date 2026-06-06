<template>
  <div class="model-config">
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>大模型配置</span>
          <el-tag type="info">{{ currentProviderName }}</el-tag>
        </div>
      </template>

      <!-- 供应商选择 -->
      <div class="provider-section">
        <h3>选择供应商</h3>
        <el-radio-group v-model="selectedProvider" @change="onProviderChange">
          <el-radio-button value="CLAUDE">
            <div class="provider-option">
              <span class="provider-name">Claude (Anthropic)</span>
            </div>
          </el-radio-button>
          <el-radio-button value="DEEPSEEK">
            <div class="provider-option">
              <span class="provider-name">DeepSeek</span>
            </div>
          </el-radio-button>
          <el-radio-button value="OPENAI">
            <div class="provider-option">
              <span class="provider-name">OpenAI</span>
            </div>
          </el-radio-button>
          <el-radio-button value="QWEN">
            <div class="provider-option">
              <span class="provider-name">通义千问 (Qwen)</span>
            </div>
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 配置表单 -->
      <el-form label-width="120px" :model="configForm" ref="configFormRef">
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="configForm.apiKey"
            type="password"
            placeholder="请输入 API Key"
            show-password
            class="config-input"
          />
        </el-form-item>

        <el-form-item label="API URL" prop="apiUrl">
          <el-input
            v-model="configForm.apiUrl"
            placeholder="API 地址"
            class="config-input"
          />
        </el-form-item>

        <el-form-item label="模型" prop="model">
          <el-select v-model="configForm.model" class="model-select">
            <el-option
              v-for="m in availableModels"
              :key="m"
              :label="m"
              :value="m"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="每次最大 Token">
          <el-slider
            v-model="configForm.maxTokens"
            :min="100"
            :max="4000"
            :step="100"
            show-input
          />
        </el-form-item>

        <el-form-item label="Temperature" v-if="selectedProvider !== 'CLAUDE'">
          <el-slider
            v-model="configForm.temperature"
            :min="0"
            :max="1"
            :step="0.1"
            show-input
          />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="configForm.isDefault" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="testing" @click="testConnection">
            <el-icon><Connection /></el-icon>
            测试连接
          </el-button>
          <el-button type="success" :loading="saving" @click="saveConfig">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
          <el-button @click="resetForm">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 配置列表 -->
      <el-divider>已保存的配置</el-divider>

      <el-table :data="configList" style="width: 100%">
        <el-table-column prop="providerName" label="供应商" />
        <el-table-column prop="model" label="模型" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.isEnabled ? 'success' : 'danger'">
              {{ row.isEnabled ? '启用' : '禁用' }}
            </el-tag>
            <el-tag v-if="row.isDefault" type="warning" style="margin-left: 8px;">
              默认
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="editConfig(row)">编辑</el-button>
            <el-button
              size="small"
              type="warning"
              @click="setDefault(row)"
              :disabled="row.isDefault"
            >
              设默认
            </el-button>
            <el-button size="small" type="danger" @click="deleteConfig(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 使用统计 -->
      <el-divider>使用统计</el-divider>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ stats.callCount }}</div>
          <div class="stat-label">调用次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.tokenCount }}</div>
          <div class="stat-label">Token 消耗</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">${{ stats.dailyCost }}</div>
          <div class="stat-label">今日成本</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">${{ stats.monthlyCost }}</div>
          <div class="stat-label">本月成本</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Check, RefreshRight } from '@element-plus/icons-vue'
import {
  getProviderPresets,
  listModelConfigs,
  saveModelConfig,
  updateModelConfig,
  deleteModelConfig,
  setDefaultConfig,
  testConnection as testConnectionApi
} from '@/api/ai'

const selectedProvider = ref('DEEPSEEK')
const availableModels = ref([])
const configList = ref([])
const testing = ref(false)
const saving = ref(false)
const editingId = ref(null)
const originalFormValues = ref(null)  // 用于重置回滚

const configForm = reactive({
  provider: 'DEEPSEEK',
  providerName: 'DeepSeek',
  apiUrl: 'https://api.deepseek.com/v1',
  apiKey: '',
  model: 'deepseek-chat',
  maxTokens: 2000,
  temperature: 0.7,
  isDefault: false,
  isEnabled: true,
  tenantId: 0
})

const stats = ref({
  callCount: 0,
  tokenCount: 0,
  dailyCost: 0.0,
  monthlyCost: 0.0
})

// 当前供应商名称
const currentProviderName = computed(() => {
  const names = {
    'CLAUDE': 'Claude (Anthropic)',
    'DEEPSEEK': 'DeepSeek',
    'OPENAI': 'OpenAI',
    'QWEN': '通义千问 (Qwen)'
  }
  return names[selectedProvider.value] || '自定义'
})

// 供应商预设数据
const providerPresets = {
  CLAUDE: {
    apiUrl: 'https://api.anthropic.com/v1/messages',
    models: ['claude-sonnet-4-6', 'claude-haiku-3.5', 'claude-opus-3']
  },
  DEEPSEEK: {
    apiUrl: 'https://api.deepseek.com/v1',
    models: ['deepseek-chat', 'deepseek-reasoner']
  },
  OPENAI: {
    apiUrl: 'https://api.openai.com/v1',
    models: ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo']
  },
  QWEN: {
    apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    models: ['qwen-turbo', 'qwen-plus', 'qwen-max']
  }
}

// 供应商切换
const onProviderChange = (provider) => {
  configForm.provider = provider
  configForm.providerName = currentProviderName.value

  const preset = providerPresets[provider]
  if (preset) {
    configForm.apiUrl = preset.apiUrl
    availableModels.value = preset.models
    configForm.model = preset.models[0]
  }
}

// 测试连接
const testConnection = async () => {
  if (!configForm.apiKey) {
    ElMessage.warning('请先输入 API Key')
    return
  }

  testing.value = true
  try {
    const res = await testConnectionApi(configForm)
    if (res.data.success) {
      ElMessage.success('连接成功！' + (res.data.response ? res.data.response.substring(0, 50) : ''))
    } else {
      ElMessage.error('连接失败：' + res.data.message)
    }
  } catch (error) {
    ElMessage.error('连接失败：' + error.message)
  } finally {
    testing.value = false
  }
}

// 保存配置
const saveConfig = async () => {
  saving.value = true
  try {
    // 创建普通对象副本，避免 Vue reactive 代理序列化问题
    const payload = { ...configForm }
    let res
    if (editingId.value) {
      res = await updateModelConfig(editingId.value, payload)
    } else {
      res = await saveModelConfig(payload)
    }

    if (res.code === 200) {
      ElMessage.success('配置保存成功！')
      resetForm()
      loadConfigs()
    } else {
      ElMessage.error('保存失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('保存失败：' + error.message)
  } finally {
    saving.value = false
  }
}

// 编辑配置
const editConfig = (row) => {
  editingId.value = row.id
  // 保存原始值快照，用于重置回滚
  originalFormValues.value = { ...row }

  // 直接设置供应商（触发 v-model），但不依赖 @change 事件
  selectedProvider.value = row.provider

  // 手动设置预设数据（相当于 onProviderChange 的部分功能）
  const preset = providerPresets[row.provider]
  if (preset) {
    availableModels.value = preset.models
  } else {
    availableModels.value = []
  }

  // 用数据库保存的值填充表单（确保不被任何事件覆盖）
  configForm.provider = row.provider
  configForm.providerName = row.providerName || currentProviderName.value
  configForm.apiUrl = row.apiUrl
  configForm.apiKey = ''  // 不回显 API Key
  configForm.model = row.model
  configForm.maxTokens = row.maxTokens || 2000
  configForm.temperature = row.temperature || 0.7
  configForm.isDefault = row.isDefault === true || row.isDefault === 1
  configForm.isEnabled = row.isEnabled === true || row.isEnabled === 1
  configForm.tenantId = row.tenantId
}

// 删除配置
const deleteConfig = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '提示', { type: 'warning' })
  } catch {
    return // 用户取消，不处理
  }

  try {
    const res = await deleteModelConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功！')
      loadConfigs()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败：' + (error.message || error))
  }
}

// 设为默认
const setDefault = async (row) => {
  try {
    const res = await setDefaultConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('已设为默认配置！')
      loadConfigs()
    }
  } catch (error) {
    ElMessage.error('设置失败：' + error.message)
  }
}

// 重置表单
const resetForm = () => {
  // 编辑模式下：恢复到编辑前的原始值
  if (editingId.value && originalFormValues.value) {
    const orig = originalFormValues.value
    Object.assign(configForm, {
      provider: orig.provider,
      providerName: orig.providerName,
      apiUrl: orig.apiUrl,
      apiKey: '',  // 不回显 API Key
      model: orig.model,
      maxTokens: orig.maxTokens || 2000,
      temperature: orig.temperature || 0.7,
      isDefault: orig.isDefault === true || orig.isDefault === 1,
      isEnabled: orig.isEnabled === true || orig.isEnabled === 1,
      tenantId: orig.tenantId
    })
    selectedProvider.value = orig.provider
    onProviderChange(orig.provider)
    return
  }

  // 新增模式下：重置为当前供应商默认预设
  editingId.value = null
  Object.assign(configForm, {
    provider: selectedProvider.value,
    providerName: currentProviderName.value,
    apiUrl: providerPresets[selectedProvider.value]?.apiUrl || '',
    apiKey: '',
    model: '',
    maxTokens: 2000,
    temperature: 0.7,
    isDefault: false,
    isEnabled: true,
    tenantId: 0
  })
  onProviderChange(selectedProvider.value)
}

// 加载配置列表
const loadConfigs = async () => {
  try {
    const res = await listModelConfigs()
    if (res.code === 200) {
      configList.value = res.data || []
    }
  } catch (error) {
    console.error('加载配置失败：', error)
  }
}

onMounted(() => {
  loadConfigs()
  onProviderChange(selectedProvider.value)
})
</script>

<style scoped>
.model-config {
    max-width: 1000px;
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

.provider-section {
    margin-bottom: 24px;
}

.provider-section h3 {
    margin-bottom: 12px;
    color: var(--el-text-color-primary);
}

.provider-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.provider-name {
    font-size: 14px;
}

.config-input {
    width: 400px;
}

.model-select {
    width: 300px;
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
</style>
