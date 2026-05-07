<template>
  <div class="wrong-questions">
    <el-card>
      <template #header>
        <span>错题记录</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的错题" name="my">
          <el-form :inline="true" class="search-form">
            <el-form-item label="学生ID">
              <el-input v-model="studentId" placeholder="输入学生ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadMyWrong">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="myWrongList" style="width: 100%" v-loading="loading">
            <el-table-column prop="questionId" label="题目ID" width="100" />
            <el-table-column prop="wrongCount" label="错误次数" width="100" />
            <el-table-column prop="lastWrongAt" label="最近错误时间" width="180" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.correctedAt ? 'success' : 'danger'">
                  {{ row.correctedAt ? '已纠错' : '待纠错' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="success" link @click="markCorrected(row)" v-if="!row.correctedAt">
                  标记纠错
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="高频错题" name="frequent">
          <el-table :data="frequentWrongList" style="width: 100%" v-loading="loading">
            <el-table-column prop="studentId" label="学生ID" width="100" />
            <el-table-column prop="questionId" label="题目ID" width="100" />
            <el-table-column prop="wrongCount" label="错误次数" width="100" />
            <el-table-column prop="lastWrongAt" label="最近错误时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWrongQuestions, getFrequentWrongQuestions, markCorrected } from '@/api/grading'

const activeTab = ref('my')
const loading = ref(false)
const studentId = ref('1')
const myWrongList = ref([])
const frequentWrongList = ref([])

const loadMyWrong = async () => {
  loading.value = true
  try {
    const res: any = await getWrongQuestions(Number(studentId.value))
    myWrongList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadFrequentWrong = async () => {
  loading.value = true
  try {
    const res: any = await getFrequentWrongQuestions(20)
    frequentWrongList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleMarkCorrected = async (row: any) => {
  await markCorrected(Number(studentId.value), row.questionId)
  ElMessage.success('已标记纠错')
  loadMyWrong()
}

onMounted(() => {
  loadMyWrong()
  loadFrequentWrong()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>