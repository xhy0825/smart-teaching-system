<template>
  <div class="exam-generate">
    <el-card>
      <template #header>
        <span>AI试卷生成</span>
      </template>

      <el-form ref="formRef" :model="generateForm" :rules="rules" label-width="100px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="generateForm.title" placeholder="请输入试卷标题" />
        </el-form-item>

        <el-form-item label="学科" prop="subject">
          <el-select v-model="generateForm.subject">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>

        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="generateForm.totalScore" :min="10" :max="200" />
        </el-form-item>

        <el-form-item label="时长(分钟)" prop="timeLimit">
          <el-input-number v-model="generateForm.timeLimit" :min="10" :max="180" />
        </el-form-item>

        <el-form-item label="试卷结构">
          <div class="structure-sections">
            <div v-for="(section, index) in sections" :key="index" class="section-item">
              <el-select v-model="section.type" placeholder="题型" style="width: 120px">
                <el-option label="选择题" value="CHOICE" />
                <el-option label="填空题" value="FILL" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="计算题" value="CALCULATION" />
              </el-select>
              <el-input-number v-model="section.count" :min="1" :max="20" placeholder="数量" />
              <el-input-number v-model="section.scoreEach" :min="1" :max="20" placeholder="每题分值" />
              <el-button type="danger" link @click="removeSection(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" link @click="addSection">
              <el-icon><Plus /></el-icon>
              添加题型
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="generating" @click="generateExam">
            <el-icon><MagicStick /></el-icon>
            AI生成试卷
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成预览 -->
    <el-card v-if="generatedPaper" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>试卷预览</span>
          <el-button type="success" @click="publishExam">发布试卷</el-button>
        </div>
      </template>

      <h3>{{ generatedPaper.title }}</h3>
      <el-table :data="generatedPaper.questions" style="width: 100%">
        <el-table-column prop="sequence" label="序号" width="60" />
        <el-table-column prop="question.content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="question.questionType" label="题型" width="100">
          <template #default="{ row }">
            {{ typeMap[row.question.questionType] }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { generatePaper, publishPaper } from '@/api/exam'

const formRef = ref<FormInstance>()
const generating = ref(false)
const generatedPaper = ref(null)

const generateForm = ref({
  title: '',
  subject: 'MATH',
  totalScore: 100,
  timeLimit: 60,
  createdBy: 1
})

const sections = ref([
  { type: 'CHOICE', count: 10, scoreEach: 4 },
  { type: 'FILL', count: 5, scoreEach: 4 }
])

const rules: FormRules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  totalScore: [{ required: true, message: '请设置总分', trigger: 'change' }]
}

const typeMap = {
  CHOICE: '选择题',
  FILL: '填空题',
  JUDGE: '判断题',
  CALCULATION: '计算题'
}

const addSection = () => {
  sections.value.push({ type: 'CHOICE', count: 5, scoreEach: 4 })
}

const removeSection = (index: number) => {
  sections.value.splice(index, 1)
}

const generateExam = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  generating.value = true
  try {
    const structure = JSON.stringify(sections.value)
    const res: any = await generatePaper({
      ...generateForm.value,
      structure
    })
    generatedPaper.value = res.data
    ElMessage.success('试卷生成成功')
  } catch (error) {
    console.error(error)
  } finally {
    generating.value = false
  }
}

const publishExam = async () => {
  if (!generatedPaper.value) return

  await publishPaper(generatedPaper.value.id)
  ElMessage.success('试卷发布成功')
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.structure-sections {
  border: 1px solid #dcdfe6;
  padding: 10px;
  border-radius: 4px;
}

.section-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
</style>