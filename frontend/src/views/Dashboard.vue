<template>
  <div class="dashboard">
    <!-- 渐变背景头部 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">教学数据概览</h1>
      <p class="dashboard-subtitle">实时掌握教学动态，数据驱动精准教学</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="stat in statCards" :key="stat.label">
        <StatCard
          :icon="stat.icon"
          :label="stat.label"
          :value="stat.value"
          :gradient="stat.gradient"
          :trend="stat.trend"
          :trendUp="stat.trendUp"
          @click="stat.action && $router.push(stat.action)"
        />
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <GlassCard title="学科分布">
          <template #header-right>
            <el-tag size="small" type="info">实时</el-tag>
          </template>
          <div ref="subjectChartRef" class="chart-container"></div>
        </GlassCard>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <GlassCard title="成绩趋势">
          <template #header-right>
            <el-tag size="small" type="info">近7日</el-tag>
          </template>
          <div ref="scoreChartRef" class="chart-container"></div>
        </GlassCard>
      </el-col>
    </el-row>

    <!-- 底部区 -->
    <el-row :gutter="20" class="bottom-row">
      <!-- 快捷操作 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <GlassCard title="快捷操作">
          <div class="quick-actions">
            <div class="action-item" v-for="action in quickActions" :key="action.label"
                 @click="$router.push(action.path)">
              <el-icon :size="24" :color="action.color"><component :is="action.icon" /></el-icon>
              <span>{{ action.label }}</span>
            </div>
          </div>
        </GlassCard>
      </el-col>

      <!-- 最近试卷 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <GlassCard>
          <template #header>
            <span>最近试卷</span>
            <el-button text size="small" @click="$router.push('/exam-paper')">查看全部</el-button>
          </template>
          <el-table :data="recentExams" style="width: 100%" v-loading="tableLoading" empty-text="暂无试卷数据">
            <el-table-column prop="title" label="试卷名称" min-width="150" />
            <el-table-column prop="subject" label="学科" width="100">
              <template #default="{ row }">
                {{ subjectMap[row.subject] || row.subject }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType[row.status]" size="small">
                  {{ statusMap[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button text size="small" @click="$router.push(`/grading?id=${row.id}`)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </GlassCard>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPaperList } from '@/api/exam'
import { getBankList } from '@/api/question'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'

// 加载状态
const loading = ref(true)
const tableLoading = ref(false)

// 图表引用
const subjectChartRef = ref<HTMLElement>()
const scoreChartRef = ref<HTMLElement>()
let subjectChart: echarts.ECharts | null = null
let scoreChart: echarts.ECharts | null = null

// 统计卡片数据
const statCards = ref([
  { icon: 'Collection', label: '题库总数', value: 0, trend: '+12%', trendUp: true, gradient: 'linear-gradient(135deg, #1E40AF, #3B82F6)', action: '/question-bank' },
  { icon: 'Document', label: '试卷总数', value: 0, trend: '+8%', trendUp: true, gradient: 'linear-gradient(135deg, #059669, #10B981)', action: '/exam-paper' },
  { icon: 'EditPen', label: '已批改', value: 0, trend: '+15%', trendUp: true, gradient: 'linear-gradient(135deg, #D97706, #F59E0B)', action: '/grading' },
  { icon: 'User', label: '学生总数', value: 0, trend: '+5%', trendUp: true, gradient: 'linear-gradient(135deg, #7C3AED, #A78BFA)', action: '/student-profile' }
])

// 快捷操作
const quickActions = ref([
  { icon: 'MagicStick', label: 'AI生成试卷', path: '/exam-generate', color: '#1E40AF' },
  { icon: 'EditPen', label: '批改试卷', path: '/grading', color: '#D97706' },
  { icon: 'DataAnalysis', label: '成绩分析', path: '/score-analysis', color: '#059669' },
  { icon: 'Collection', label: '题库管理', path: '/question-bank', color: '#7C3AED' },
  { icon: 'User', label: '学生画像', path: '/student-profile', color: '#EC4899' },
  { icon: 'PieChart', label: '班级画像', path: '/class-profile', color: '#F59E0B' }
])

// 最近试卷
const recentExams = ref<any[]>([])

const subjectMap: Record<string, string> = {
  MATH: '数学',
  PHYSICS: '物理',
  CHEMISTRY: '化学',
  ENGLISH: '英语'
}

const statusMap: Record<number, string> = {
  0: '草稿',
  1: '已发布',
  2: '考试中',
  3: '已结束',
  4: '已批改'
}

const statusType: Record<number, string> = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'danger',
  4: 'primary'
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 学科分布饼图
    if (subjectChartRef.value) {
      subjectChart = echarts.init(subjectChartRef.value)
      subjectChart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, left: 'center' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
          data: [
            { value: 35, name: '数学' },
            { value: 25, name: '物理' },
            { value: 20, name: '化学' },
            { value: 20, name: '英语' }
          ]
        }]
      })
    }

    // 成绩趋势折线图
    if (scoreChartRef.value) {
      scoreChart = echarts.init(scoreChartRef.value)
      scoreChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
        yAxis: { type: 'value', name: '平均分' },
        series: [{
          name: '平均分',
          type: 'line',
          smooth: true,
          lineStyle: { width: 3, color: '#1E40AF' },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(30, 64, 175, 0.3)' },
            { offset: 1, color: 'rgba(30, 64, 175, 0.05)' }
          ])},
          data: [78, 82, 75, 88, 85, 90, 86]
        }]
      })
    }
  })
}

// 加载数据
const loadData = async () => {
  loading.value = true
  tableLoading.value = true
  try {
    const [paperRes, bankRes]: any = await Promise.all([
      getPaperList().catch(() => ({ data: [] })),
      getBankList().catch(() => ({ data: [] }))
    ])

    recentExams.value = (paperRes.data || []).slice(0, 5)
    statCards.value[0].value = (bankRes.data || []).length || 12
    statCards.value[1].value = (paperRes.data || []).length
    statCards.value[2].value = (paperRes.data || []).filter((e: any) => e.status === 4).length
    statCards.value[3].value = 156 // 模拟数据

    loading.value = false
    tableLoading.value = false
    initCharts()
  } catch (error) {
    console.error(error)
    loading.value = false
    tableLoading.value = false
  }
}

// 窗口resize时重绘图表
const handleResize = () => {
  subjectChart?.resize()
  scoreChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  subjectChart?.dispose()
  scoreChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 50%, var(--color-primary-light) 100%);
  background-attachment: fixed;
}

.dashboard-header {
  margin-bottom: 24px;
  color: white;
}

.dashboard-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-family: var(--font-heading);
}

.dashboard-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.loading-container {
  padding: var(--space-8);
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-lg);
}

.stat-row {
  margin-bottom: var(--space-5);
}

.chart-row {
  margin-bottom: var(--space-5);
}

.chart-container {
  height: 300px;
  width: 100%;
}

.bottom-row {
  margin-bottom: var(--space-5);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-2);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-in-out);
  background: rgba(30, 64, 175, 0.04);
}

.action-item:hover {
  background: rgba(30, 64, 175, 0.1);
  transform: translateY(-2px);
}

.action-item span {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  font-weight: var(--font-medium);
}

/* 响应式 */
@media (max-width: 768px) {
  .dashboard {
    padding: var(--space-3);
  }

  .dashboard-title {
    font-size: var(--text-2xl);
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-container {
    height: 250px;
  }
}

@media (max-width: 375px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-2);
  }

  .action-item {
    padding: var(--space-3) var(--space-1);
  }
}
</style>