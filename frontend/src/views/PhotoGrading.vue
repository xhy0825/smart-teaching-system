<template>
  <div class="photo-grading">
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>拍照批改</span>
          <el-tag type="success">AI Vision 识别</el-tag>
        </div>
      </template>

      <!-- 上传区域 -->
      <el-upload
        class="upload-area"
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :show-file-list="false"
        accept="image/*"
      >
        <el-icon :size="48"><Camera /></el-icon>
        <div class="el-upload__text">
          点击或拖拽上传手写答案照片
        </div>
      </el-upload>

      <!-- 预览区域 -->
      <div v-if="imageUrl" class="preview">
        <img :src="imageUrl" alt="手写答案" class="preview-img" />
        <el-button type="primary" :loading="loading" @click="gradePhoto">
          <el-icon><MagicStick /></el-icon>
          开始批改
        </el-button>
      </div>

      <!-- 批改结果 -->
      <div v-if="result" class="result">
        <el-alert
          :type="result.isCorrect === 1 ? 'success' : result.isCorrect === 2 ? 'warning' : 'error'"
          :title="'得分：' + result.score + ' 分'"
        >
          <template #default>
            <p>识别准确率：{{ (result.accuracy * 100).toFixed(1) }}%</p>
            <p v-if="result.needReview" class="review-warning">
              ⚠️ 准确率低于阈值，需人工复核
            </p>
            <p>{{ result.analysis }}</p>
          </template>
        </el-alert>
        <el-button @click="reset">继续批改</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, MagicStick } from '@element-plus/icons-vue'

const imageUrl = ref('')
const loading = ref(false)
const result = ref(null)
const currentFile = ref(null)

const handleFileChange = (uploadFile) => {
  currentFile.value = uploadFile.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target.result
  }
  reader.readAsDataURL(uploadFile.raw)
}

const gradePhoto = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请先上传照片')
    return
  }

  loading.value = true
  result.value = null

  try {
    const formData = new FormData()
    formData.append('photo', currentFile.value)

    // TODO: 调用后端拍照批改 API
    // const res = await axios.post('/api/grading/photo', formData)

    // 模拟返回
    setTimeout(() => {
      result.value = {
        score: 85,
        isCorrect: 1,
        accuracy: 0.92,
        needReview: false,
        analysis: '答案正确，步骤完整'
      }
      loading.value = false
    }, 1500)
  } catch (error) {
    ElMessage.error('批改失败：' + error.message)
    loading.value = false
  }
}

const reset = () => {
  imageUrl.value = ''
  result.value = null
  currentFile.value = null
}
</script>

<style scoped>
.photo-grading {
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

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
}

.upload-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    border: 2px dashed var(--el-border-color);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.upload-area:hover {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
}

.preview {
    margin-top: 20px;
    text-align: center;
}

.preview-img {
    max-width: 100%;
    max-height: 400px;
    border-radius: 8px;
    margin-bottom: 16px;
}

.result {
    margin-top: 20px;
}

.review-warning {
    color: #e6a23c;
    font-weight: bold;
}
</style>
