<template>
  <div class="exam-template">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>试卷模板</span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon>
            新增模板
          </el-button>
        </div>
      </template>

      <el-table :data="templateList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="subject" label="学科">
          <template #default="{ row }">
            {{ subjectMap[row.subject] }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="timeLimit" label="时长(分钟)" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="warning" link @click="editTemplate(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="editMode ? '编辑模板' : '新增模板'" width="600px">
      <el-form ref="formRef" :model="templateForm" :rules="rules" label-width="80px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="templateForm.subject">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="英语" value="ENGLISH" />
          </el-select>
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="templateForm.totalScore" :min="10" :max="200" />
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="templateForm.timeLimit" :min="10" :max="180" />
        </el-form-item>
        <el-form-item label="试卷结构">
          <el-input v-model="templateForm.structure" type="textarea" :rows="4" placeholder="JSON格式试卷结构" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTemplateList, createTemplate, updateTemplate, deleteTemplate } from '@/api/exam'

const loading = ref(false)
const showDialog = ref(false)
const editMode = ref(false)
const templateList = ref([])
const formRef = ref<FormInstance>()
const editId = ref(0)

const templateForm = ref({
  name: '',
  subject: 'MATH',
  totalScore: 100,
  timeLimit: 60,
  structure: '[{"section":"选择题","type":"CHOICE","count":10,"scoreEach":4}]'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
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
    const res: any = await getTemplateList()
    templateList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editTemplate = (row: any) => {
  editMode.value = true
  editId.value = row.id
  templateForm.value = {
    name: row.name,
    subject: row.subject,
    totalScore: row.totalScore,
    timeLimit: row.timeLimit,
    structure: row.structure
  }
  showDialog.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该模板？', '提示', { type: 'warning' })
  await deleteTemplate(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const saveTemplate = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  if (editMode.value) {
    await updateTemplate(editId.value, templateForm.value)
    ElMessage.success('更新成功')
  } else {
    await createTemplate(templateForm.value)
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
</style>