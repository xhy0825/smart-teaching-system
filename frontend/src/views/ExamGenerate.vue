<template>
  <div class="exam-generate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI试卷生成</span>
          <el-tag type="success">智能出题</el-tag>
        </div>
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

        <el-form-item label="生成策略">
          <el-radio-group v-model="generateForm.generateStrategy">
            <el-radio value="SIMPLE">简单随机</el-radio>
            <el-radio value="SMART">智能分布</el-radio>
            <el-radio value="AI">AI推荐</el-radio>
            <el-radio value="PERSONALIZED">个性化出题</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 个性化出题选项 -->
        <el-form-item label="目标学生" v-if="generateForm.generateStrategy === 'PERSONALIZED'">
          <el-select v-model="generateForm.targetStudentId" placeholder="选择学生" filterable @change="loadStudentProfile">
            <el-option v-for="stu in studentList" :key="stu.id" :label="stu.name" :value="stu.id" />
          </el-select>
          <el-button type="primary" link @click="showStudentSelector">
            <el-icon><Plus /></el-icon>
            加载学生列表
          </el-button>
        </el-form-item>

        <!-- 学生薄弱知识点显示 -->
        <el-form-item label="薄弱知识点" v-if="generateForm.generateStrategy === 'PERSONALIZED' && studentWeakPoints.length > 0">
          <div class="weak-points-info">
            <el-tag type="warning" v-for="kp in studentWeakPoints" :key="kp" class="weak-tag">
              {{ kp }}
            </el-tag>
            <span class="weak-hint">（自动覆盖以上薄弱知识点）</span>
          </div>
        </el-form-item>

        <el-form-item label="难度分布" v-if="generateForm.generateStrategy !== 'SIMPLE'">
          <div class="difficulty-config">
            <div class="difficulty-item">
              <span class="diff-label">简单:</span>
              <el-slider v-model="difficultyConfig.easy" :max="100" :format-tooltip="(val: number) => val + '%'" />
              <el-tag type="success">{{ difficultyConfig.easy }}%</el-tag>
            </div>
            <div class="difficulty-item">
              <span class="diff-label">中等:</span>
              <el-slider v-model="difficultyConfig.medium" :max="100" :format-tooltip="(val: number) => val + '%'" />
              <el-tag type="warning">{{ difficultyConfig.medium }}%</el-tag>
            </div>
            <div class="difficulty-item">
              <span class="diff-label">困难:</span>
              <el-slider v-model="difficultyConfig.hard" :max="100" :format-tooltip="(val: number) => val + '%'" />
              <el-tag type="danger">{{ difficultyConfig.hard }}%</el-tag>
            </div>
            <div class="difficulty-total">
              <span>难度比例总和: {{ difficultyConfig.easy + difficultyConfig.medium + difficultyConfig.hard }}%</span>
              <el-button type="primary" size="small" @click="balanceDifficulty" v-if="difficultyConfig.easy + difficultyConfig.medium + difficultyConfig.hard !== 100">
                自动平衡
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="知识点覆盖" v-if="generateForm.generateStrategy === 'AI'">
          <el-select v-model="generateForm.knowledgePoints" multiple placeholder="选择知识点" filterable>
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
              <span class="section-total">小计: {{ section.count * section.scoreEach }}分</span>
              <el-button type="danger" link @click="removeSection(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div class="structure-summary">
              <span>总题数: {{ totalQuestionCount }}题</span>
              <span>分值合计: {{ totalScoreFromSections }}分</span>
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
            {{ generateForm.generateStrategy === 'AI' ? 'AI智能生成' : generateForm.generateStrategy === 'SMART' ? '智能分布生成' : '随机生成' }}
          </el-button>
          <el-button @click="resetForm">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成预览 -->
    <el-card v-if="generatedPaper" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>试卷预览 - {{ generatedPaper.title }}</span>
          <div>
            <el-button type="primary" @click="exportPreview">
              <el-icon><Download /></el-icon>
              导出预览
            </el-button>
            <el-button type="success" @click="publishExam">
              <el-icon><Promotion /></el-icon>
              发布试卷
            </el-button>
          </div>
        </div>
      </template>

      <div class="paper-info">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学科">{{ subjectMap[generatedPaper.subject] }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ generatedPaper.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="时长">{{ generatedPaper.timeLimit }}分钟</el-descriptions-item>
          <el-descriptions-item label="题目数">{{ generatedPaper.questions?.length || 0 }}题</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-table :data="generatedPaper.questions" style="width: 100%; margin-top: 16px">
        <el-table-column prop="sequence" label="序号" width="60" />
        <el-table-column prop="question.content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="question.questionType" label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.question.questionType)">
              {{ typeMap[row.question.questionType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="question.difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.question.difficulty)">
              {{ difficultyMap[row.question.difficulty] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80">
          <template #default="{ row }">
            <span class="score-value">{{ row.score }}分</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 难度分布统计 -->
      <div class="stats-section" v-if="generatedPaper.questions">
        <h4>难度分布统计</h4>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8" :span="8">
            <el-statistic title="简单题目" :value="difficultyStats.easy">
              <template #suffix>题</template>
            </el-statistic>
          </el-col>
          <el-col :xs="24" :sm="8" :span="8">
            <el-statistic title="中等题目" :value="difficultyStats.medium">
              <template #suffix>题</template>
            </el-statistic>
          </el-col>
          <el-col :xs="24" :sm="8" :span="8">
            <el-statistic title="困难题目" :value="difficultyStats.hard">
              <template #suffix>题</template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { generatePaper, publishPaper } from '@/api/exam'

const formRef = ref<FormInstance>()
const generating = ref(false)
const generatedPaper = ref<any>(null)

const generateForm = ref({
  title: '',
  subject: 'MATH',
  totalScore: 100,
  timeLimit: 60,
  createdBy: 1,
  generateStrategy: 'SMART',
  knowledgePoints: [] as string[],
  difficultyDistribution: {} as Record<number, number>,
  targetStudentId: null as number | null
})

const difficultyConfig = reactive({
  easy: 30,
  medium: 50,
  hard: 20
})

const sections = ref([
  { type: 'CHOICE', count: 10, scoreEach: 4 },
  { type: 'FILL', count: 5, scoreEach: 4 }
])

const studentList = ref<any[]>([])
const studentWeakPoints = ref<string[]>([])

const rules: FormRules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  totalScore: [{ required: true, message: '请设置总分', trigger: 'change' }]
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

const totalQuestionCount = computed(() => {
  return sections.value.reduce((sum, s) => sum + s.count, 0)
})

const totalScoreFromSections = computed(() => {
  return sections.value.reduce((sum, s) => sum + s.count * s.scoreEach, 0)
})

const difficultyStats = computed(() => {
  if (!generatedPaper.value?.questions) return { easy: 0, medium: 0, hard: 0 }
  const questions = generatedPaper.value.questions
  return {
    easy: questions.filter((q: any) => q.question?.difficulty === 1).length,
    medium: questions.filter((q: any) => q.question?.difficulty === 2).length,
    hard: questions.filter((q: any) => q.question?.difficulty === 3).length
  }
})

const balanceDifficulty = () => {
  const total = difficultyConfig.easy + difficultyConfig.medium + difficultyConfig.hard
  if (total === 0) {
    difficultyConfig.easy = 30
    difficultyConfig.medium = 50
    difficultyConfig.hard = 20
    return
  }
  difficultyConfig.easy = Math.round(difficultyConfig.easy * 100 / total)
  difficultyConfig.medium = Math.round(difficultyConfig.medium * 100 / total)
  difficultyConfig.hard = 100 - difficultyConfig.easy - difficultyConfig.medium
}

const addSection = () => {
  sections.value.push({ type: 'CHOICE', count: 5, scoreEach: 4 })
}

const removeSection = (index: number) => {
  sections.value.splice(index, 1)
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

const generateExam = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  // 验证分值匹配
  if (totalScoreFromSections.value !== generateForm.value.totalScore) {
    ElMessage.warning(`试卷结构总分(${totalScoreFromSections.value}分)与设置总分(${generateForm.value.totalScore}分)不一致`)
  }

  // 个性化出题需要选择学生
  if (generateForm.value.generateStrategy === 'PERSONALIZED' && !generateForm.value.targetStudentId) {
    ElMessage.warning('个性化出题需要选择目标学生')
    return
  }

  generating.value = true
  try {
    const structure = JSON.stringify(sections.value)

    // 构建难度分布参数
    const difficultyDistribution = {
      1: difficultyConfig.easy / 100,
      2: difficultyConfig.medium / 100,
      3: difficultyConfig.hard / 100
    }

    // 合成知识点（个性化出题时自动包含薄弱知识点）
    let finalKnowledgePoints = generateForm.value.knowledgePoints
    if (generateForm.value.generateStrategy === 'PERSONALIZED' && studentWeakPoints.value.length > 0) {
      finalKnowledgePoints = [...generateForm.value.knowledgePoints, ...studentWeakPoints.value]
    }

    const requestData = {
      ...generateForm.value,
      structure,
      difficultyDistribution,
      knowledgePoints: finalKnowledgePoints.length > 0 ? finalKnowledgePoints : undefined
    }

    // 根据策略选择不同的API
    let res: any
    if (generateForm.value.generateStrategy === 'PERSONALIZED') {
      res = await request.post('/exam-paper/generate-personalized', requestData)
    } else {
      res = await generatePaper(requestData)
    }

    generatedPaper.value = res.data
    ElMessage.success('试卷生成成功')
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const publishExam = async () => {
  if (!generatedPaper.value) return

  try {
    await publishPaper(generatedPaper.value.id)
    ElMessage.success('试卷发布成功')
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
  }
}

const exportPreview = () => {
  ElMessage.info('导出功能开发中')
}

const resetForm = () => {
  formRef.value?.resetFields()
  sections.value = [
    { type: 'CHOICE', count: 10, scoreEach: 4 },
    { type: 'FILL', count: 5, scoreEach: 4 }
  ]
  difficultyConfig.easy = 30
  difficultyConfig.medium = 50
  difficultyConfig.hard = 20
  generatedPaper.value = null
  studentWeakPoints.value = []
  generateForm.value.targetStudentId = null
}

const showStudentSelector = async () => {
  try {
    // 加载班级列表
    const classRes: any = await request.get('/class/list')
    if (classRes.data && classRes.data.length > 0) {
      // 加载第一个班级的学生
      const classId = classRes.data[0].id
      const stuRes: any = await request.get(`/student/class/${classId}`)
      studentList.value = stuRes.data || []
      if (studentList.value.length > 0) {
        ElMessage.success(`已加载 ${studentList.value.length} 名学生`)
      }
    }
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  }
}

const loadStudentProfile = async () => {
  if (!generateForm.value.targetStudentId) {
    studentWeakPoints.value = []
    return
  }
  try {
    const res: any = await request.get(`/student-profile/${generateForm.value.targetStudentId}`)
    if (res.data && res.data.knowledgePoints) {
      // 提取薄弱知识点（掌握率低于75%的）
      studentWeakPoints.value = res.data.knowledgePoints
        .filter((kp: any) => kp.masteryRate < 75)
        .map((kp: any) => kp.knowledgePoint)
      if (studentWeakPoints.value.length > 0) {
        ElMessage.info(`检测到 ${studentWeakPoints.value.length} 个薄弱知识点`)
      }
    }
  } catch (error) {
    console.error(error)
  }
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

.section-total {
  color: #409eff;
  font-size: 14px;
}

.structure-summary {
  display: flex;
  gap: 20px;
  padding: 8px 0;
  color: #606266;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}

.difficulty-config {
  padding: 10px;
}

.difficulty-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.diff-label {
  width: 60px;
  font-weight: 500;
}

.difficulty-total {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  color: #909399;
}

.paper-info {
  margin-bottom: 16px;
}

.score-value {
  font-weight: 600;
  color: #409eff;
}

.stats-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 4px;
}

.stats-section h4 {
  margin-bottom: 16px;
  color: #303133;
}

.weak-points-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.weak-tag {
  margin-right: 8px;
}

.weak-hint {
  color: #909399;
  font-size: 12px;
}
</style>