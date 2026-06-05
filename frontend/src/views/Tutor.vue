<template>
  <div class="ai-tutor" :class="{ 'mobile': isMobile, 'tablet': isTablet }">
    <!-- 移动端顶部栏 -->
    <div v-if="isMobile" class="mobile-header">
      <el-button class="menu-btn" @click="showSidebar = !showSidebar">
        <el-icon><Menu /></el-icon>
      </el-button>
      <span class="page-title">AI 助教</span>
      <el-select
        v-model="selectedModel"
        size="small"
        class="model-select-mobile"
        placeholder="选择模型"
      >
        <el-option
          v-for="m in availableModels"
          :key="m"
          :label="m"
          :value="m"
        />
      </el-select>
      <el-button size="small" type="primary" @click="newConversation">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- 侧边栏 -->
    <div
      class="sidebar"
      :class="{ collapsed: sidebarCollapsed, mobile: isMobile, 'show-mobile': showSidebar }"
    >
      <div class="sidebar-header">
        <span v-if="!sidebarCollapsed || isMobile">对话列表</span>
        <el-button
          v-if="!sidebarCollapsed && !isMobile"
          size="small"
          class="collapse-btn"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <el-icon><DArrowLeft /></el-icon>
        </el-button>
      </div>

      <div class="conversation-list" v-if="!sidebarCollapsed || isMobile">
        <div v-if="conversations.length === 0" class="empty-sidebar">
          <el-icon :size="24"><ChatDot /></el-icon>
          <p>还没有对话</p>
          <el-button size="small" type="primary" @click="newConversation">
            新建对话
          </el-button>
        </div>

        <div
          v-for="(conv, idx) in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: conv.id === currentConversationId }"
          @click="switchConversation(conv.id)"
        >
          <div class="conv-title">{{ conv.title || '新对话' }}</div>
          <div class="conv-meta">
            <span class="conv-time">{{ formatTime(conv.updatedAt) }}</span>
            <el-button
              size="small"
              type="danger"
              class="delete-btn"
              @click.stop="deleteConversation(conv.id, idx)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="!isMobile" class="sidebar-footer">
        <el-button class="new-btn" type="primary" @click="newConversation">
          <el-icon><Plus /></el-icon>
          <span v-if="!sidebarCollapsed">新建对话</span>
        </el-button>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat">
      <!-- 桌面/平板顶部栏 -->
      <div v-if="!isMobile" class="chat-header">
        <el-button
          v-if="sidebarCollapsed"
          size="small"
          class="expand-btn"
          @click="sidebarCollapsed = false"
        >
          <el-icon><DArrowRight /></el-icon>
        </el-button>
        <span class="header-title">AI 助教</span>
        <div class="header-actions">
          <el-select
            v-model="selectedModel"
            size="small"
            class="model-select"
            placeholder="选择模型"
          >
            <el-option
              v-for="m in availableModels"
              :key="m"
              :label="m"
              :value="m"
            />
          </el-select>
          <el-button size="small" @click="clearConversation">
            清空对话
          </el-button>
        </div>
      </div>

      <!-- 聊天内容 -->
      <div class="chat-container" ref="chatContainer">
        <!-- 空状态 -->
        <div v-if="messages.length === 0 && !loading" class="empty-state">
          <el-icon :size="48"><ChatRound /></el-icon>
          <h3>欢迎使用 AI 助教</h3>
          <p>我可以解答教学问题、提供备课建议、分析学生数据</p>
          <el-button type="primary" @click="focusInput">
            开始新对话
          </el-button>
        </div>

        <!-- 消息列表 -->
        <template v-else>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div class="message-content">
              <div class="role">{{ msg.role === 'user' ? '您' : 'AI助教' }}</div>
              <div class="text">
                <!-- 加载指示器 -->
                <template v-if="msg.loading">
                  <div class="typing-indicator">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                  </div>
                </template>
                <!-- 普通消息 -->
                <template v-else>
                  {{ msg.content }}
                </template>
              </div>
            </div>
          </div>
        </template>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="error-alert">
          <el-alert type="error" :closable="true" @close="errorMsg = ''">
            {{ errorMsg }}
          </el-alert>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入教学问题，如：如何讲解三角函数？"
          class="message-input"
          @keydown.enter.exact="sendMessage"
        />
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!inputMessage.trim()"
          @click="sendMessage"
        >
          <el-icon><Position /></el-icon>
          发送
        </el-button>
      </div>
    </div>

    <!-- 移动端侧边栏遮罩 -->
    <div
      v-if="isMobile && showSidebar"
      class="sidebar-overlay"
      @click="showSidebar = false"
    ></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Position, Menu, ChatDot, ChatRound, Delete, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import axios from 'axios'

const inputMessage = ref('')
const loading = ref(false)
const messages = ref([])
const errorMsg = ref('')
const selectedModel = ref('')
const availableModels = ref([])

// 对话管理
const conversations = ref([])
const currentConversationId = ref('')
const sidebarCollapsed = ref(false)
const showSidebar = ref(false)

// 响应式断点
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1440)
const isMobile = computed(() => windowWidth.value <= 768)
const isTablet = computed(() => windowWidth.value > 768 && windowWidth.value <= 1024)

// 聊天容器引用
const chatContainer = ref(null)

// 加载可用模型（从后端配置）
const loadAvailableModels = async () => {
  try {
    const res = await axios.get('/api/ai/configs')
    if (res.data.success) {
      const configs = res.data.data || []
      // 收集所有可用模型
      const models = new Set()
      configs.forEach(config => {
        if (config.isEnabled !== 0) {
          models.add(config.model)
          if (config.availableModels) {
            try {
              const available = JSON.parse(config.availableModels)
              available.forEach(m => models.add(m))
            } catch (e) {}
          }
        }
      })
      availableModels.value = Array.from(models)
      if (availableModels.value.length > 0 && !selectedModel.value) {
        selectedModel.value = availableModels.value[0]
      }
    }
  } catch (error) {
    console.error('加载模型失败：', error)
  }
}

// 加载对话列表（从 localStorage）
const loadConversations = () => {
  try {
    const saved = localStorage.getItem('ai-tutor-conversations')
    if (saved) {
      conversations.value = JSON.parse(saved)
    }
  } catch (e) {
    conversations.value = []
  }

  // 如果没有对话，创建一个默认的
  if (conversations.value.length === 0) {
    newConversation()
  } else {
    // 恢复最后一个对话
    const last = conversations.value[conversations.value.length - 1]
    switchConversation(last.id)
  }
}

// 保存对话列表到 localStorage
const saveConversations = () => {
  try {
    localStorage.setItem('ai-tutor-conversations', JSON.stringify(conversations.value))
  } catch (e) {
    console.warn('保存对话失败：', e)
  }
}

// 新建对话
const newConversation = () => {
  const newConv = {
    id: 'conv_' + Date.now(),
    title: '新对话',
    messages: [
      { role: 'assistant', content: '您好！我是您的 AI 助教，可以解答教学问题、提供备课建议、分析学生数据。' }
    ],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  conversations.value.push(newConv)
  switchConversation(newConv.id)
  saveConversations()
}

// 切换对话
const switchConversation = (convId) => {
  currentConversationId.value = convId
  const conv = conversations.value.find(c => c.id === convId)
  if (conv) {
    messages.value = conv.messages || []
    // 更新当前对话的更新时间
    conv.updatedAt = new Date().toISOString()
    saveConversations()
  }
  // 移动端自动隐藏侧边栏
  if (isMobile.value) {
    showSidebar.value = false
  }
}

// 删除对话
const deleteConversation = async (convId, idx) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    conversations.value.splice(idx, 1)
    saveConversations()

    // 如果删除的是当前对话，切换到最后一个
    if (convId === currentConversationId.value) {
      if (conversations.value.length > 0) {
        switchConversation(conversations.value[conversations.value.length - 1].id)
      } else {
        newConversation()
      }
    }
  } catch (e) {
    // 用户取消
  }
}

// 清空当前对话
const clearConversation = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有消息吗？', '提示', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
    messages.value = [
      { role: 'assitant', content: '您好！我是您的 AI 助教，可以解答教学问题、提供备课建议、分析学生数据。' }
    ]
    // 更新对话
    const conv = conversations.value.find(c => c.id === currentConversationId.value)
    if (conv) {
      conv.messages = messages.value
      conv.title = '新对话'
      saveConversations()
    }
  } catch (e) {
    // 用户取消
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) {
    return
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content: inputMessage.value })

  // 更新对话标题（如果是第一条消息）
  const conv = conversations.value.find(c => c.id === currentConversationId.value)
  if (conv && conv.messages.length <= 2) {
    conv.title = inputMessage.value.slice(0, 30) + (inputMessage.value.length > 30 ? '...' : '')
  }

  loading.value = true
  const userMessage = inputMessage.value
  inputMessage.value = ''

  // 添加加载中的消息（带 typing indicator）
  const loadingMsgIdx = messages.value.length
  messages.value.push({ role: 'assitant', content: '', loading: true })

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 调用后端 AI 助教接口
    const res = await axios.post('/api/ai-tutor/chat', {
      conversationId: currentConversationId.value,
      message: userMessage,
      model: selectedModel.value || undefined
    })

    if (res.data.success) {
      // 替换加载消息为真实回复
      messages.value[loadingMsgIdx] = {
        role: 'assitant',
        content: res.data.response || '抱歉，我没有收到回复。'
      }

      // 更新对话
      if (conv) {
        conv.messages = messages.value
        conv.updatedAt = new Date().toISOString()
        saveConversations()
      }
    } else {
      // 替换加载消息为错误
      messages.value[loadingMsgIdx] = {
        role: 'assitant',
        content: '抱歉，发生了错误。'
      }
      errorMsg.value = res.data.error || '发送失败，请稍后重试。'
    }
  } catch (error) {
    console.error('发送失败：', error)
    // 替换加载消息为错误
    messages.value[loadingMsgIdx] = {
      role: 'assitant',
      content: '抱歉，网络错误，请检查连接。'
    }
    errorMsg.value = '发送失败：' + (error.message || '网络错误')
  } finally {
    loading.value = false
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 聚焦输入框
const focusInput = () => {
  const input = document.querySelector('.message-input textarea')
  if (input) input.focus()
}

// 格式化时间
const formatTime = (isoStr) => {
  if (!isoStr) return ''
  const date = new Date(isoStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 窗口大小变化监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 生命周期
onMounted(() => {
  loadAvailableModels()
  loadConversations()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.ai-tutor {
    max-width: 1440px;
    margin: 40px auto;
    padding: 20px;
    height: calc(100vh - 80px);
    display: flex;
    gap: 0;
    position: relative;
}

/* 移动端样式 */
.ai-tutor.mobile {
    margin: 0;
    padding: 0;
    height: 100vh;
}

.mobile-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--el-border-color);
    position: sticky;
    top: 0;
    z-index: 100;
}

.mobile-header .page-title {
    flex: 1;
    font-weight: bold;
    font-size: 14px;
}

.model-select-mobile {
    width: 120px;
}

/* 侧边栏 */
.sidebar {
    width: 320px;
    min-width: 320px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-right: 1px solid var(--el-border-color);
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
    overflow: hidden;
}

.sidebar.collapsed {
    width: 0;
    min-width: 0;
    border-right: none;
}

.sidebar.mobile {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 200;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
}

.sidebar.mobile.show-mobile {
    transform: translateX(0);
}

.sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color);
    font-weight: bold;
    font-size: 14px;
}

.collapse-btn,
.expand-btn {
    border: none;
    background: transparent;
}

.sidebar-footer {
    padding: 16px;
    border-top: 1px solid var(--el-border-color);
}

.new-btn {
    width: 100%;
}

.conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
}

.empty-sidebar {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 20px;
    color: var(--el-text-color-secondary);
}

.conversation-item {
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: background 0.2s;
}

.conversation-item:hover {
    background: var(--el-fill-color-light);
}

.conversation-item.active {
    background: var(--el-color-primary-light-9);
    border-left: 3px solid var(--el-color-primary);
}

.conv-title {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.conv-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.conv-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}

.delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
    padding: 2px;
}

.conversation-item:hover .delete-btn {
    opacity: 1;
}

/* 主聊天区域 */
.main-chat {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.chat-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--el-border-color);
}

.header-title {
    flex: 1;
    font-weight: bold;
    font-size: 16px;
}

.header-actions {
    display: flex;
    gap: 8px;
    align-items: center;
}

.model-select {
    width: 200px;
}

.chat-container {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    height: 100%;
    color: var(--el-text-color-secondary);
}

.empty-state h3 {
    margin: 0;
    color: var(--el-text-color-primary);
}

.empty-state p {
    margin: 0;
    text-align: center;
    max-width: 300px;
}

/* 消息样式 */
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

.text {
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
}

/* Typing indicator */
.typing-indicator {
    display: flex;
    gap: 4px;
    padding: 8px 0;
}

.dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--el-text-color-secondary);
    animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
    0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
    40% { transform: scale(1.2); opacity: 1; }
}

/* 错误提示 */
.error-alert {
    margin: 12px 20px;
}

/* 输入区域 */
.input-area {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid var(--el-border-color);
    align-items: flex-end;
}

.message-input {
    flex: 1;
}

.message-input :deep(.el-textarea__inner) {
    border-radius: 8px;
    resize: none;
}

/* 平板适配 */
@media (max-width: 1024px) {
    .sidebar {
        width: 260px;
        min-width: 260px;
    }
}

/* 移动端适配 */
@media (max-width: 768px) {
    .main-chat {
        height: calc(100vh - 56px);
    }

    .chat-header {
        display: none;
    }

    .input-area {
        padding: 12px;
    }

    .message-content {
        max-width: 90%;
    }
}

/* 侧边栏遮罩（移动端）*/
.sidebar-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: 199;
}
</style>
