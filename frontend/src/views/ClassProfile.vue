<template>
  <div class="class-profile">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">班级画像</span>
      </template>
    </el-page-header>

    <div class="profile-content" v-loading="loading">
      <!-- 班级选择 -->
      <el-card class="select-card" shadow="hover">
        <el-form :inline="true">
          <el-form-item label="选择班级">
            <el-select v-model="selectedClassId" placeholder="请选择班级" @change="loadClassProfile" clearable>
              <el-option v-for="cls in classList" :key="cls.id" :label="cls.name" :value="cls.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <template v-if="stats">
        <!-- 基础统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-statistic title="平均分" :value="stats.baseStats.avgScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最高分" :value="stats.baseStats.maxScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="最低分" :value="stats.baseStats.minScore" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="参考人数" :value="stats.baseStats.studentCount" />
          </el-col>
        </el-row>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-statistic title="及格率" :value="stats.baseStats.passRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="优秀率" :value="stats.baseStats.excellentRate * 100" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已批改" :value="stats.baseStats.gradedCount" />
          </el-col>
        </el-row>

        <!-- 分数段分布图 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>分数段分布</span>
          </template>
          <div ref="chartRef" style="height: 300px"></div>
        </el-card>

        <!-- 知识点掌握表格 -->
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span>知识点掌握分布</span>
          </template>
          <el-table :data="stats.knowledgeMastery" style="width: 100%">
            <el-table-column prop="knowledgePoint" label="知识点" />
            <el-table-column prop="avgMasteryRate" label="平均掌握率">
              <template #default="{ row }">
                <el-progress :percentage="row.avgMasteryRate" />
              </template>
            </el-table-column>
            <el-table-column prop="weakStudentCount" label="薄弱人数" />
            <el-table-column prop="level" label="掌握等级">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 学生列表 -->
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span>学生列表</span>
          </template>
          <el-table :data="studentList" style="width: 100%">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="studentNo" label="学号" />
            <el-table-column prop="avgScore" label="平均分" />
            <el-table-column prop="highestScore" label="最高分" />
            <el-table-column prop="lowestScore" label="最低分" />
            <el-table-column prop="ranking" label="排名" />
            <el-table-column prop="wrongCount" label="错题数" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewProfile(row.id)">查看画像</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>

      <el-empty v-else-if="!loading" description="请选择班级查看画像" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getClassProfileStats } from '@/api/class-profile'
import { getClasList } from '@/api/user'
import { getStudentProfilesByClass } from '@/api/student'
import type { ClassProfileStats } from '@/api/class-profile'

const router = useRouter()
const loading = ref(false)
const stats = ref<ClassProfileStats | null>(null)
const selectedClassId = ref<number | null>(null)
const classList = ref<any[]>([])
const studentList = ref<any[]>([])
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const goBack = () => {
  router.push('/dashboard')
}

const loadClasList = async () => {
  try {
    const res: any = await getClasList()
    classList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadClassProfile = async () => {
  if (!selectedClassId.value) {
    stats.value = null
    studentList.value = []
    return
  }
  loading.value = true
  try {
    const res: any = await getClassProfileStats(selectedClassId.value)
    stats.value = res.data
    // 加载学生列表
    const studentRes: any = await getStudentProfilesByClass(selectedClassId.value)
    studentList.value = studentRes.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取班级画像失败')
    stats.value = null
  } finally {
    loading.value = false
  }
}

const viewProfile = (studentId: number) => {
  router.push(`/student-profile?studentId=${studentId}`)
}

const initChart = () => {
  if (!chartRef.value || !stats.value?.distribution) return
  nextTick(() => {
    if (!chartRef.value) return
    if (chartInstance) {
      chartInstance.dispose()
    }
    chartInstance = echarts.init(chartRef.value)
    const labels = stats.value.distribution.map((d: any) => d.range)
    const data = stats.value.distribution.map((d: any) => d.count)
    chartInstance.setOption({
      title: { text: '分数段分布', left: 'center' },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: data, itemStyle: { color: '#409eff' } }]
    })
  })
}

watch(() => stats.value?.distribution, () => { initChart() })

const getLevelType = (level: string) => {
  switch (level) {
    case '优秀': return 'success'
    case '良好': return 'primary'
    case '一般': return 'warning'
    case '薄弱': return 'danger'
    default: return 'info'
  }
}

onMounted(() => {
  loadClasList()
})
</script>

<style scoped>
.class-profile { padding: 20px; }
.profile-content { margin-top: 20px; }
.select-card { margin-bottom: 20px; }
.stats-row { margin-bottom: 20px; }
.chart-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
</style>
