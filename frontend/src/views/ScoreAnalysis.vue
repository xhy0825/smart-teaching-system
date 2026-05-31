<template>
  <div class="score-analysis">
    <el-card>
      <template #header>
        <span>成绩分析</span>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="试卷">
          <el-select v-model="selectedPaper" placeholder="选择试卷" filterable clearable style="width: 300px">
            <el-option v-for="paper of paperList" :key="paper.id" :label="paper.title" :value="paper.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="selectedClass" placeholder="选择班级" filterable clearable style="width: 300px">
            <el-option v-for="cls of classList" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAnalysis">查询</el-button>
          <el-button type="success" @click="analyze">生成分析</el-button>
        </el-form-item>
      </el-form>

      <div v-if="analysis" class="analysis-content">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="平均分" :value="analysis.avgScore" />
          </el-col>
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="最高分" :value="analysis.maxScore" />
          </el-col>
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="最低分" :value="analysis.minScore" />
          </el-col>
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="参考人数" :value="analysis.studentCount" />
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="及格率" :value="analysis.passRate * 100" suffix="%" />
          </el-col>
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="优秀率" :value="analysis.excellentRate * 100" suffix="%" />
          </el-col>
          <el-col :xs="12" :sm="6" :span="6">
            <el-statistic title="已批改" :value="analysis.gradedCount" />
          </el-col>
        </el-row>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>题目得分分析</span>
          </template>
          <el-table :data="analysis.questionAnalysis" style="width: 100%">
            <el-table-column prop="sequence" label="序号" width="60" />
            <el-table-column prop="maxScore" label="满分" width="80" />
            <el-table-column prop="avgScore" label="平均得分" width="100" />
            <el-table-column prop="correctRate" label="正确率" width="100">
              <template #default="{ row }">
                {{ (row.correctRate * 100).toFixed(1) }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaperList } from '@/api/exam'
import { getScoreAnalysis, analyzeClassScores } from '@/api/grading'
import { getClasList } from '@/api/user'

const paperList = ref<any[]>([])
const classList = ref<any[]>([])
const selectedPaper = ref<number>()
const selectedClass = ref<number>()
const analysis = ref(null)

const loadPapers = async () => {
  const res: any = await getPaperList()
  paperList.value = res.data
  // 等待 DOM 更新（生成 el-option）
  await nextTick()
  // 默认选中第一个试卷
  if (paperList.value.length > 0) {
    selectedPaper.value = Number(paperList.value[0].id)
  }
}

const loadClasses = async () => {
  try {
    const res: any = await getClasList()
    classList.value = res.data || []
    // 等待 DOM 更新（生成 el-option）
    await nextTick()
    // 默认选中第一个班级
    if (classList.value.length > 0) {
      selectedClass.value = Number(classList.value[0].id)
    }
  } catch (error) {
    console.error('加载班级失败', error)
  }
}

const loadAnalysis = async () => {
  if (!selectedPaper.value) {
    ElMessage.warning('请选择试卷')
    return
  }
  const res: any = await getScoreAnalysis(selectedPaper.value, selectedClass.value)
  analysis.value = res.data
}

const analyze = async () => {
  if (!selectedPaper.value) {
    ElMessage.warning('请选择试卷')
    return
  }
  await analyzeClassScores(selectedPaper.value, selectedClass.value)
  ElMessage.success('分析完成')
  loadAnalysis()
}

onMounted(() => {
  loadPapers()
  loadClasses()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.analysis-content {
  margin-top: 20px;
}
</style>