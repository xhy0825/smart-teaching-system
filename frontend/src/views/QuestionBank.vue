<template>
  <div class="question-bank">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题库列表</span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon>
            新增题库
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="学科">
          <el-select v-model="searchSubject" placeholder="选择学科" clearable>
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="bankList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="题库名称" />
        <el-table-column prop="subject" label="学科">
          <template #default="{ row }">
            {{ subjectMap[row.subject] }}
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewQuestions(row)">
              查看题目
            </el-button>
            <el-button type="warning" link @click="editBank(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteBank(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="editMode ? '编辑题库' : '新增题库'" width="500px">
      <el-form ref="formRef" :model="bankForm" :rules="rules" label-width="80px">
        <el-form-item label="题库名称" prop="name">
          <el-input v-model="bankForm.name" />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="bankForm.subject">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="bankForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBank">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { getBankList, getBankListBySubject, createBank, updateBank, deleteBank } from '@/api/question'

const router = useRouter()
const loading = ref(false)
const showDialog = ref(false)
const editMode = ref(false)
const bankList = ref([])
const searchSubject = ref('')
const formRef = ref<FormInstance>()
const editId = ref(0)

const bankForm = ref({
  name: '',
  subject: 'MATH',
  description: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入题库名称', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }]
}

const subjectMap = {
  MATH: '数学',
  PHYSICS: '物理',
  CHEMISTRY: '化学',
  ENGLISH: '英语'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = searchSubject.value
      ? await getBankListBySubject(searchSubject.value)
      : await getBankList()
    bankList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewQuestions = (row: any) => {
  router.push(`/question?bankId=${row.id}`)
}

const editBank = (row: any) => {
  editMode.value = true
  editId.value = row.id
  bankForm.value = {
    name: row.name,
    subject: row.subject,
    description: row.description || ''
  }
  showDialog.value = true
}

const deleteBank = async (row: any) => {
  await ElMessageBox.confirm('确定删除该题库？', '提示', { type: 'warning' })
  await deleteBank(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const saveBank = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  if (editMode.value) {
    await updateBank(editId.value, bankForm.value)
    ElMessage.success('更新成功')
  } else {
    await createBank(bankForm.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
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

.search-form {
  margin-bottom: 20px;
}
</style>