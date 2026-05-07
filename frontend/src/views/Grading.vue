<template>
  <div class="grading">
    <el-card>
      <template #header>
        <span>试卷批改</span>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="试卷">
          <el-select v-model="selectedPaper" placeholder="选择试卷" @change="loadAnswerSheets">
            <el-option v-for="paper in paperList" :key="paper.id" :label="paper.title" :value="paper.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="answerSheets" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewAnswers(row)">查看答案</el-button>
            <el-button type="success" link @click="gradeSheet(row)" v-if="row.status === 1">
              批改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 答案详情对话框 -->
    <el-dialog v-model="showAnswers" title="答题详情" width="700px">
      <el-table :data="answers" style="width: 100%">
        <el-table-column prop="sequence" label="序号" width="60" />
        <el-table-column prop="questionContent" label="题目" show-overflow-tooltip />
        <el-table-column prop="studentAnswer" label="学生答案" />
        <el-table-column prop="score" label="得分" width="80" />
        <el-table-column prop="isCorrect" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect === 1 ? 'success' : 'danger'">
              {{ row.isCorrect === 1 ? '正确' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaperList } from '@/api/exam'
import { getAnswerSheetList, getAnswerList, gradeAnswerSheet } from '@/api/grading'

const loading = ref(false)
const showAnswers = ref(false)
const paperList = ref([])
const answerSheets = ref([])
const answers = ref([])
const selectedPaper = ref(null)

const statusMap = {
  0: '未提交',
  1: '已提交',
  2: '批改中',
  3: '已批改'
}

const statusType = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success'
}

const loadPapers = async () => {
  const res: any = await getPaperList()
  paperList.value = res.data
}

const loadAnswerSheets = async () => {
  if (!selectedPaper.value) return
  loading.value = true
  try {
    const res: any = await getAnswerSheetList(selectedPaper.value)
    answerSheets.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewAnswers = async (row: any) => {
  const res: any = await getAnswerList(row.id)
  answers.value = res.data
  showAnswers.value = true
}

const gradeSheet = async (row: any) => {
  await gradeAnswerSheet(row.id, 1)
  ElMessage.success('批改完成')
  loadAnswerSheets()
}

onMounted(() => {
  loadPapers()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>