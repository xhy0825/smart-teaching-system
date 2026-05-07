<template>
  <div class="exam-paper">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>试卷列表</span>
        </div>
      </template>

      <el-table :data="paperList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="试卷名称" />
        <el-table-column prop="subject" label="学科">
          <template #default="{ row }">
            {{ subjectMap[row.subject] }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewPaper(row)">查看</el-button>
            <el-button type="success" link @click="publishPaper(row)" v-if="row.status === 0">
              发布
            </el-button>
            <el-button type="danger" link @click="deletePaper(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 试卷详情对话框 -->
    <el-dialog v-model="showDetail" title="试卷详情" width="800px">
      <div v-if="currentPaper">
        <h3>{{ currentPaper.title }}</h3>
        <p>学科: {{ subjectMap[currentPaper.subject] }} | 总分: {{ currentPaper.totalScore }} | 时长: {{ currentPaper.timeLimit }}分钟</p>

        <el-table :data="currentPaper.questions" style="width: 100%; margin-top: 20px">
          <el-table-column prop="sequence" label="序号" width="60" />
          <el-table-column prop="question.content" label="题目内容" show-overflow-tooltip />
          <el-table-column prop="question.questionType" label="题型" width="100">
            <template #default="{ row }">
              {{ typeMap[row.question.questionType] }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="80" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaperList, getPaperDetail, publishPaper, deletePaper } from '@/api/exam'

const loading = ref(false)
const showDetail = ref(false)
const paperList = ref([])
const currentPaper = ref(null)

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

const typeMap = {
  CHOICE: '选择题',
  FILL: '填空题',
  JUDGE: '判断题',
  CALCULATION: '计算题'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getPaperList()
    paperList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewPaper = async (row: any) => {
  const res: any = await getPaperDetail(row.id)
  currentPaper.value = res.data
  showDetail.value = true
}

const handlePublish = async (row: any) => {
  await ElMessageBox.confirm('确定发布该试卷？', '提示', { type: 'warning' })
  await publishPaper(row.id)
  ElMessage.success('发布成功')
  loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该试卷？', '提示', { type: 'warning' })
  await deletePaper(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>