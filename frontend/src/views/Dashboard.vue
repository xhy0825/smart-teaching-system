<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon"><Collection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.questionBankCount }}</div>
              <div class="stat-label">题库总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.examCount }}</div>
              <div class="stat-label">试卷总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon"><EditPen /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.gradedCount }}</div>
              <div class="stat-label">已批改试卷</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/exam-generate')">
              <el-icon><MagicStick /></el-icon>
              AI生成试卷
            </el-button>
            <el-button type="success" @click="$router.push('/grading')">
              <el-icon><EditPen /></el-icon>
              批改试卷
            </el-button>
            <el-button type="warning" @click="$router.push('/score-analysis')">
              <el-icon><DataAnalysis /></el-icon>
              成绩分析
            </el-button>
            <el-button type="info" @click="$router.push('/question-bank')">
              <el-icon><Collection /></el-icon>
              题库管理
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近试卷</span>
          </template>
          <el-table :data="recentExams" style="width: 100%">
            <el-table-column prop="title" label="试卷名称" />
            <el-table-column prop="subject" label="学科">
              <template #default="{ row }">
                {{ subjectMap[row.subject] }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="statusType[row.status]">
                  {{ statusMap[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPaperList } from '@/api/exam'

const stats = ref({
  questionBankCount: 0,
  examCount: 0,
  gradedCount: 0,
  studentCount: 0
})

const recentExams = ref([])

const subjectMap = {
  MATH: '数学',
  PHYSICS: '物理',
  CHEMISTRY: '化学',
  ENGLISH: '英语'
}

const statusMap = {
  0: '草稿',
  1: '已发布',
  2: '考试中',
  3: '已结束',
  4: '已批改'
}

const statusType = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'danger',
  4: 'primary'
}

onMounted(async () => {
  try {
    const res: any = await getPaperList()
    recentExams.value = res.data.slice(0, 5)
    stats.value.examCount = res.data.length
    stats.value.gradedCount = res.data.filter(e => e.status === 4).length
  } catch (error) {
    console.error(error)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 120px;
}
</style>