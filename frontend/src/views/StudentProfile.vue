<template>
  <div class="student-profile">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">学生画像</span>
      </template>
    </el-page-header>

    <div class="profile-content" v-loading="loading">
      <!-- 学生选择 -->
      <el-card class="select-card" shadow="hover">
        <el-form :inline="true">
          <el-form-item label="选择班级">
            <el-select v-model="selectedClassId" placeholder="请选择班级" @change="loadStudents" clearable>
              <el-option v-for="cls in classList" :key="cls.id" :label="cls.name" :value="cls.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择学生">
            <el-select v-model="selectedStudentId" placeholder="请选择学生" @change="loadProfile" clearable>
              <el-option v-for="stu in studentList" :key="stu.id" :label="stu.name" :value="stu.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <template v-if="profile">
        <!-- 基本信息卡片 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="info-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>基本信息</span>
                </div>
              </template>
              <div class="student-avatar">
                <el-avatar :size="80" :icon="UserFilled" />
              </div>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
                <el-descriptions-item label="学号">{{ profile.studentNo }}</el-descriptions-item>
                <el-descriptions-item label="班级">{{ profile.className || '未知班级' }}</el-descriptions-item>
                <el-descriptions-item label="性别">
                  {{ profile.gender === 1 ? '男' : profile.gender === 2 ? '女' : '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="年龄">{{ profile.age || '未知' }} 岁</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <!-- 成绩概览 -->
          <el-col :span="8">
            <el-card class="score-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><DataAnalysis /></el-icon>
                  <span>成绩概览</span>
                </div>
              </template>
              <div class="score-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ profile.examCount || 0 }}</div>
                  <div class="stat-label">考试次数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ profile.avgScore || 0 }}</div>
                  <div class="stat-label">平均分</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value high">{{ profile.highestScore || 0 }}</div>
                  <div class="stat-label">最高分</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value low">{{ profile.lowestScore || 0 }}</div>
                  <div class="stat-label">最低分</div>
                </div>
              </div>
              <div class="rank-info" v-if="profile.ranking">
                <el-tag type="success">班级排名: 第 {{ profile.ranking }} 名</el-tag>
              </div>
            </el-card>
          </el-col>

          <!-- 错题统计 -->
          <el-col :span="8">
            <el-card class="wrong-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Warning /></el-icon>
                  <span>错题统计</span>
                </div>
              </template>
              <div class="wrong-stats">
                <el-progress
                  :percentage="correctedPercentage"
                  :color="correctedPercentage > 80 ? '#67c23a' : correctedPercentage > 50 ? '#e6a23c' : '#f56c6c'"
                >
                  <span>纠错进度</span>
                </el-progress>
                <div class="wrong-info">
                  <span>总错题: {{ profile.totalWrongCount || 0 }} 道</span>
                  <span>已纠错: {{ profile.correctedCount || 0 }} 道</span>
                </div>
              </div>
              <div class="wrong-types" v-if="profile.wrongQuestionTypes">
                <div v-for="type in profile.wrongQuestionTypes" :key="type.questionType" class="type-item">
                  <span>{{ type.typeName }}</span>
                  <el-tag size="small">{{ type.count }} 题</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 知识点掌握情况 -->
        <el-card class="knowledge-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Collection /></el-icon>
              <span>知识点掌握情况</span>
            </div>
          </template>
          <el-table :data="profile.knowledgePoints" style="width: 100%" v-if="profile.knowledgePoints">
            <el-table-column prop="knowledgePoint" label="知识点" />
            <el-table-column prop="masteryRate" label="掌握率">
              <template #default="{ row }">
                <el-progress :percentage="row.masteryRate" :color="getMasteryColor(row.masteryRate)" />
              </template>
            </el-table-column>
            <el-table-column prop="level" label="掌握等级">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="correctCount" label="正确次数" />
            <el-table-column prop="totalCount" label="总次数" />
          </el-table>
          <el-empty v-else description="暂无知识点数据" />
        </el-card>

        <!-- 成绩趋势 -->
        <el-card class="trend-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>成绩趋势</span>
            </div>
          </template>
          <div ref="trendChart" class="trend-chart" v-if="profile.scoreTrends && profile.scoreTrends.length > 0"></div>
          <el-empty v-else description="暂无成绩趋势数据" />
        </el-card>
      </template>

      <el-empty v-else-if="!loading" description="请选择学生查看画像" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, DataAnalysis, Warning, Collection, TrendCharts } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const profile = ref<any>(null)
const selectedClassId = ref<number | null>(null)
const selectedStudentId = ref<number | null>(null)
const classList = ref<any[]>([])
const studentList = ref<any[]>([])

// 纠错进度百分比
const correctedPercentage = computed(() => {
  if (!profile.value || profile.value.totalWrongCount === 0) return 0
  return Math.round((profile.value.correctedCount / profile.value.totalWrongCount) * 100)
})

const goBack = () => {
  router.push('/dashboard')
}

const loadClassList = async () => {
  try {
    const res: any = await request.get('/class/list')
    classList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadStudents = async () => {
  if (!selectedClassId.value) {
    studentList.value = []
    return
  }
  try {
    const res: any = await request.get(`/student/class/${selectedClassId.value}`)
    studentList.value = res.data || []
  } catch (error) {
    console.error(error)
    studentList.value = []
  }
}

const loadProfile = async () => {
  if (!selectedStudentId.value) {
    profile.value = null
    return
  }
  loading.value = true
  try {
    const res: any = await request.get(`/student-profile/${selectedStudentId.value}`)
    profile.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || '获取学生画像失败')
    profile.value = null
  } finally {
    loading.value = false
  }
}

const getMasteryColor = (rate: number) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 75) return '#409eff'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

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
  loadClassList()
})
</script>

<style scoped>
.student-profile {
  padding: 20px;
}

.profile-content {
  margin-top: 20px;
}

.select-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.student-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.score-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-value.high {
  color: #67c23a;
}

.stat-value.low {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.rank-info {
  text-align: center;
  padding: 16px;
}

.wrong-stats {
  padding: 16px;
}

.wrong-info {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  color: #606266;
}

.wrong-types {
  margin-top: 16px;
  padding: 0 16px;
}

.type-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.type-item:last-child {
  border-bottom: none;
}

.knowledge-card,
.trend-card {
  margin-top: 20px;
}

.trend-chart {
  height: 300px;
}
</style>