<template>
  <div class="ai-tutor">
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>AI 助教</span>
          <el-tag type="success">在线</el-tag>
        </div>
      </template>

      <div class="chat-container">
        <!-- 对话历史 -->
        <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
          <div class="message-content">
            <div class="role">{{ msg.role === 'user' ? '您' : 'AI助教' }}</div>
            <div class="text">{{ msg.content }}</div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            placeholder="输入教学问题，如：如何讲解三角函数？"
            class="message-input"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" :loading="loading" @click="sendMessage">
            <el-icon><Position /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Position } from '@element-plus/icons-vue'
import axios from 'axios'

const inputMessage = ref('')
const loading = ref(false)
const messages = ref([
  { role: 'assistant', content: '您好！我是您的 AI 助教，可以解答教学问题、提供备课建议、分析学生数据。' }
])

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content: inputMessage.value })

  loading.value = true
  const userMessage = inputMessage.value
  inputMessage.value = ''

  try {
    // TODO: 调用后端 AI 助教接口
    // const res = await axios.post('/api/ai-tutor/chat', {
    //   conversationId: 'conv_001',
    //   message: userMessage
    // })

    // 模拟返回
    setTimeout(() => {
      messages.value.push({
        role: 'assitant',
        content: `关于"${userMessage}"，我的建议是...（模拟 AI 回复）`
      })
      loading.value = false
    }, 1000)
  } catch (error) {
    ElMessage.error('发送失败：' + error.message)
    loading.value = false
  }
}
</script>

<style scoped>
.ai-tutor {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
}

.glass-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    height: 80vh;
    display: flex;
    flex-direction: column;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
}

.chat-container {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}

.message {
    margin-bottom: 16px;
}

.message.user {
    text-align: right;
}

.message-content {
    display: inline-block;
    padding: 12px 16px;
    border-radius: 12px;
    max-width: 80%;
}

.user .message-content {
    background: var(--el-color-primary);
    color: white;
}

.assitant .message-content {
    background: var(--el-color-info-light-9);
    color: var(--el-text-color-primary);
}

.role {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 4px;
}

.input-area {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid var(--el-border-color);
}

.message-input {
    flex: 1;
}

.message-input :deep(.el-input__inner) {
    height: 48px;
    border-radius: 8px;
}
</style>
