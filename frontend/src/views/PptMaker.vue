<template>
  <div class="ppt-maker">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>PPT制作</span>
          <el-tag type="primary">备课工具</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="pptForm" :rules="rules" label-width="100px">
        <el-form-item label="PPT标题" prop="title">
          <el-input v-model="pptForm.title" placeholder="请输入PPT标题" />
        </el-form-item>

        <el-form-item label="学科" prop="subject">
          <el-select v-model="pptForm.subject">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>

        <el-form-item label="模板类型" prop="templateType">
          <el-radio-group v-model="pptForm.templateType">
            <el-radio value="LESSON">
              <el-icon><Reading /></el-icon> 课堂教案
            </el-radio>
            <el-radio value="EXAM">
              <el-icon><Document /></el-icon> 考试试卷
            </el-radio>
            <el-radio value="SUMMARY">
              <el-icon><Finished /></el-icon> 知识总结
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="知识点">
          <el-select v-model="pptForm.knowledgePoints" multiple placeholder="选择知识点" filterable>
            <el-option label="负数运算" value="负数" />
            <el-option label="乘法计算" value="乘法" />
            <el-option label="加法运算" value="加法" />
            <el-option label="圆的周长" value="圆周长" />
            <el-option label="三角形面积" value="三角形" />
            <el-option label="方程求解" value="方程" />
            <el-option label="函数图像" value="函数" />
            <el-option label="概率统计" value="概率" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择题目">
          <el-button type="primary" link @click="showQuestionSelector">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
          <div v-if="selectedQuestions.length > 0" class="selected-questions">
            <el-tag v-for="q in selectedQuestions" :key="q.id" closable @close="removeQuestion(q.id)">
              {{ q.content.substring(0, 30) }}...
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item label="自定义内容">
          <el-input v-model="pptForm.customContent" type="textarea" :rows="4" placeholder="输入补充教学内容" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="generating" @click="generatePpt">
            <el-icon><MagicStick /></el-icon>
            生成PPT
          </el-button>
          <el-button @click="resetForm">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目选择弹窗 -->
    <el-dialog v-model="questionDialogVisible" title="选择题目" width="60%">
      <el-table :data="availableQuestions" @selection-change="handleQuestionSelect">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.questionType)">{{ typeMap[row.questionType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)">{{ difficultyMap[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmQuestions">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 生成结果预览 -->
    <el-card v-if="generatedPpt" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>PPT预览 - {{ generatedPpt.title }}</span>
          <div>
            <el-button type="primary" @click="exportPpt">
              <el-icon><Download /></el-icon>
              导出PPT
            </el-button>
            <el-button type="danger" @click="deleteGeneratedPpt">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </template>

      <div class="ppt-info">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学科">{{ subjectMap[generatedPpt.subject] }}</el-descriptions-item>
          <el-descriptions-item label="模板类型">{{ templateTypeMap[generatedPpt.templateType] }}</el-descriptions-item>
          <el-descriptions-item label="页数">{{ generatedPpt.pageCount }}页</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ generatedPpt.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="slides-preview">
        <el-carousel :autoplay="false" height="400px" indicator-position="outside">
          <el-carousel-item v-for="slide in generatedPpt.slides" :key="slide.pageIndex">
            <div class="slide-content">
              <h3>{{ slide.title }}</h3>
              <div class="slide-body">
                <pre>{{ slide.content }}</pre>
              </div>
              <div v-if="slide.questionIds && slide.questionIds.length > 0" class="slide-meta">
                <el-tag size="small">包含题目 {{ slide.questionIds.length }} 道</el-tag>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-card>

    <!-- 历史PPT列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>历史PPT记录</span>
      </template>
      <el-table :data="pptHistory" v-loading="loadingHistory">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="subject" label="学科" width="100">
          <template #default="{ row }">{{ subjectMap[row.subject] }}</template>
        </el-table-column>
        <el-table-column prop="templateType" label="类型" width="100">
          <template #default="{ row }">{{ templateTypeMap[row.templateType] }}</template>
        </el-table-column>
        <el-table-column prop="pageCount" label="页数" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewPpt(row.id)">查看</el-button>
            <el-button type="danger" link @click="deletePptRecord(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Reading, Document, Finished, Plus, MagicStick, RefreshRight, Download, Delete } from '@element-plus/icons-vue'
import { generatePpt, getPptList, getPptDetail, deletePpt } from '@/api/ppt'
import request from '@/utils/request'

const formRef = ref<FormInstance>()
const generating = ref(false)
const loadingHistory = ref(false)
const generatedPpt = ref<any>(null)
const pptHistory = ref<any[]>([])
const questionDialogVisible = ref(false)
const availableQuestions = ref<any[]>([])
const selectedQuestions = ref<any[]>([])
const tempSelectedQuestions = ref<any[]>([])

const pptForm = ref({
  title: '',
  subject: 'MATH',
  templateType: 'LESSON',
  knowledgePoints: [] as string[],
  questionIds: [] as number[],
  customContent: '',
  createdBy: 1
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入PPT标题', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
}

const typeMap: Record<string, string> = {
  CHOICE: '选择题',
  FILL: '填空题',
  JUDGE: '判断题',
  CALCULATION: '计算题'
}

const difficultyMap: Record<number, string> = {
  1: '简单',
  2: '中等',
  3: '困难'
}

const subjectMap: Record<string, string> = {
  MATH: '数学',
  PHYSICS: '物理',
  CHEMISTRY: '化学',
  ENGLISH: '英语'
}

const templateTypeMap: Record<string, string> = {
  LESSON: '课堂教案',
  EXAM: '考试试卷',
  SUMMARY: '知识总结'
}

const getTypeTagType = (type: string) => {
  switch (type) {
    case 'CHOICE': return 'primary'
    case 'FILL': return 'success'
    case 'JUDGE': return 'warning'
    case 'CALCULATION': return 'danger'
    default: return 'info'
  }
}

const getDifficultyTagType = (difficulty: number) => {
  switch (difficulty) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

const showQuestionSelector = async () => {
  try {
    const res: any = await request.get(`/question/bank/1`)
    availableQuestions.value = res.data || []
    questionDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取题目列表失败')
  }
}

const handleQuestionSelect = (selection: any[]) => {
  tempSelectedQuestions.value = selection
}

const confirmQuestions = () => {
  selectedQuestions.value = [...selectedQuestions.value, ...tempSelectedQuestions.value]
  pptForm.value.questionIds = selectedQuestions.value.map(q => q.id)
  questionDialogVisible.value = false
  tempSelectedQuestions.value = []
}

const removeQuestion = (questionId: number) => {
  selectedQuestions.value = selectedQuestions.value.filter(q => q.id !== questionId)
  pptForm.value.questionIds = selectedQuestions.value.map(q => q.id)
}

const generatePptAction = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  generating.value = true
  try {
    const res: any = await generatePpt(pptForm.value)
    generatedPpt.value = res.data
    ElMessage.success('PPT生成成功')
    loadPptHistory()
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  selectedQuestions.value = []
  generatedPpt.value = null
}

const exportPpt = () => {
  ElMessage.info('PPT导出功能开发中')
}

const deleteGeneratedPpt = async () => {
  if (!generatedPpt.value) return
  try {
    await deletePpt(generatedPpt.value.id)
    ElMessage.success('删除成功')
    generatedPpt.value = null
    loadPptHistory()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const viewPpt = async (id: number) => {
  try {
    const res: any = await getPptDetail(id)
    generatedPpt.value = res.data
  } catch (error) {
    ElMessage.error('获取PPT详情失败')
  }
}

const deletePptRecord = async (id: number) => {
  try {
    await deletePpt(id)
    ElMessage.success('删除成功')
    loadPptHistory()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const loadPptHistory = async () => {
  loadingHistory.value = true
  try {
    const res: any = await getPptList()
    pptHistory.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loadingHistory.value = false
  }
}

onMounted(() => {
  loadPptHistory()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-questions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ppt-info {
  margin-bottom: 16px;
}

.slides-preview {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.slide-content {
  height: 100%;
  padding: 20px;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.slide-content h3 {
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.slide-body {
  flex: 1;
  overflow: auto;
}

.slide-body pre {
  white-space: pre-wrap;
  font-family: inherit;
  color: #606266;
  line-height: 1.8;
}

.slide-meta {
  margin-top: 10px;
  text-align: right;
}
</style>