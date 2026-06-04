<template>
  <div class="simple-generate">
    <el-card class="glass-card">
      <div class="simple-form">
        <el-input
          v-model="topic"
          placeholder="请输入考试主题，如：高一数学第一次月考"
          class="topic-input"
          @keyup.enter="generate"
        />
        <el-button type="primary" :loading="loading" @click="generate">
          <el-icon><MagicStick /></el-icon>
          生成试卷
        </el-button>
      </div>

      <div v-if="result" class="result">
        <el-alert type="success" :title="'生成成功！共 ' + result.count + ' 道题'" />
        <el-button @click="reset">继续出题</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'

const topic = ref('')
const loading = ref(false)
const result = ref(null)

const generate = async () => {
  if (!topic.value.trim()) {
    ElMessage.warning('请输入考试主题')
    return
  }

  loading.value = true
  result.value = null

  try {
    // TODO: 调用后端极简出卷 API
    // 模拟生成
    setTimeout(() => {
      result.value = { count: 10 }
      loading.value = false
    }, 2000)
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
    loading.value = false
  }
}

const reset = () => {
  topic.value = ''
  result.value = null
}
</script>

<style scoped>
.simple-generate {
    max-width: 600px;
    margin: 40px auto;
    padding: 20px;
}

.glass-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.simple-form {
    display: flex;
    gap: 12px;
    align-items: center;
}

.topic-input {
    flex: 1;
}

.topic-input :deep(.el-input__inner) {
    height: 48px;
    font-size: 16px;
    border-radius: 8px;
}

.result {
    margin-top: 20px;
    text-align: center;
}
</style>
