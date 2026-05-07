<template>
  <div class="question">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon>
            新增题目
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="学科">
          <el-select v-model="searchForm.subject" placeholder="选择学科">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="searchForm.type" placeholder="选择题型" clearable>
            <el-option label="选择题" value="CHOICE" />
            <el-option label="填空题" value="FILL" />
            <el-option label="判断题" value="JUDGE" />
            <el-option label="计算题" value="CALCULATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="选择难度" clearable>
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="questionList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="{ row }">
            {{ typeMap[row.questionType] }}
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="difficultyType[row.difficulty]">
              {{ difficultyMap[row.difficulty] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="warning" link @click="editQuestion(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="editMode ? '编辑题目' : '新增题目'" width="700px">
      <el-form ref="formRef" :model="questionForm" :rules="rules" label-width="80px">
        <el-form-item label="题库" prop="bankId">
          <el-select v-model="questionForm.bankId">
            <el-option v-for="bank in bankList" :key="bank.id" :label="bank.name" :value="bank.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="questionForm.subject">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="questionForm.questionType">
            <el-option label="选择题" value="CHOICE" />
            <el-option label="填空题" value="FILL" />
            <el-option label="判断题" value="JUDGE" />
            <el-option label="计算题" value="CALCULATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="questionForm.difficulty">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项" v-if="questionForm.questionType === 'CHOICE'">
          <el-input v-model="questionForm.options" type="textarea" :rows="2" placeholder="JSON格式: {A:'选项A',B:'选项B',C:'选项C',D:'选项D'}" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="questionForm.answer" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="questionForm.answerAnalysis" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { getBankList, queryQuestions, getQuestionList, createQuestion, updateQuestion, deleteQuestion } from '@/api/question'

const route = useRoute()
const loading = ref(false)
const showDialog = ref(false)
const editMode = ref(false)
const questionList = ref([])
const bankList = ref([])
const formRef = ref<FormInstance>()
const editId = ref(0)

const searchForm = ref({
  subject: 'MATH',
  type: '',
  difficulty: null as number | null
})

const questionForm = ref({
  bankId: null as number | null,
  subject: 'MATH',
  questionType: 'CHOICE',
  difficulty: 2,
  content: '',
  options: '',
  answer: '',
  answerAnalysis: ''
})

const rules: FormRules = {
  bankId: [{ required: true, message: '请选择题库', trigger: 'change' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }]
}

const typeMap = {
  CHOICE: '选择题',
  FILL: '填空题',
  JUDGE: '判断题',
  CALCULATION: '计算题'
}

const difficultyMap = {
  1: '简单',
  2: '中等',
  3: '困难'
}

const difficultyType = {
  1: 'success',
  2: 'warning',
  3: 'danger'
}

const loadBanks = async () => {
  const res: any = await getBankList()
  bankList.value = res.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await queryQuestions(
      searchForm.value.subject,
      searchForm.value.type,
      searchForm.value.difficulty
    )
    questionList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editQuestion = (row: any) => {
  editMode.value = true
  editId.value = row.id
  questionForm.value = {
    bankId: row.bankId,
    subject: row.subject,
    questionType: row.questionType,
    difficulty: row.difficulty,
    content: row.content,
    options: row.options || '',
    answer: row.answer,
    answerAnalysis: row.answerAnalysis || ''
  }
  showDialog.value = true
}

const deleteQuestion = async (row: any) => {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const saveQuestion = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  if (editMode.value) {
    await updateQuestion(editId.value, questionForm.value)
    ElMessage.success('更新成功')
  } else {
    await createQuestion(questionForm.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

onMounted(() => {
  loadBanks()
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>