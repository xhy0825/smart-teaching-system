<template>
  <div class="score-analysis">
    <el-card>
      <template #header>
        <span>成绩分析</span>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="试卷">
          <el-select v-model="selectedPaper" placeholder="选择试卷">
            <el-option v-for="paper in paperList" :key="paper.id" :label="paper.title" :value="paper.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="selectedClass" placeholder="选择班级">
            <el-option label="默认班级" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAnalysis">查询</el-button>
          <el-button type="success" @click="analyze">生成分析</el-button>
        </el-form-item>
      </el-form>

      <div v-if="analysis" class="analysis-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="平均分" :value="analysis.avgScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最高分" :value="analysis.maxScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最低分" :value="analysis.minScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="参考人数" :value="analysis.studentCount" />
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="6">
            <el-statistic title="及格率" :value="analysis.passRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="优秀率" :value="analysis.excellentRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaperList } from '@/api/exam'
import { getScoreAnalysis, analyzeClassScores } from '@/api/grading'

const paperList = ref([])
const selectedPaper = ref(null)
const selectedClass = ref(1)
const analysis = ref(null)

const loadPapers = async () => {
  const res: any = await getPaperList()
  paperList.value = res.data
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