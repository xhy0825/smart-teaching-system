<template>
  <div class="class-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            添加班级
          </el-button>
        </div>
      </template>

      <el-table :data="classList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="班级名称" width="150" />
        <el-table-column prop="gradeId" label="年级" width="120">
          <template #default="{ row }">
            <el-tag>{{ getGradeName(row.gradeId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生人数" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ row.studentCount || 0 }}人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="showStudents(row)">
              <el-icon><UserFilled /></el-icon>
              学生列表
            </el-button>
            <el-button type="warning" link @click="showEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteClass(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑班级对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="classForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="classForm.name" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="classForm.gradeId" placeholder="选择年级" filterable clearable style="width: 300px">
            <el-option v-for="grade of gradeList" :key="grade.id" :label="grade.name" :value="grade.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 学生列表对话框 -->
    <el-dialog :title="`${currentClass?.name || ''} - 学生列表`" v-model="studentDialogVisible" width="800px">
      <div class="student-dialog-header">
        <el-button type="primary" size="small" @click="showAddStudentDialog">
          <el-icon><Plus /></el-icon>
          添加学生
        </el-button>
      </div>
      <el-table :data="studentList" v-loading="studentLoading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : 'danger'">
              {{ row.gender === 1 ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在读' : '离校' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="warning" link @click="showEditStudentDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteStudent(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog :title="studentDialogTitle" v-model="studentFormDialogVisible" width="500px">
      <el-form :model="studentForm" :rules="studentRules" ref="studentFormRef" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="studentForm.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="studentForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="studentForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studentFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStudentForm" :loading="studentSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, UserFilled, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const classList = ref<any[]>([])
const gradeList = ref<any[]>([])
const gradeMap = ref<Record<number, string>>({})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const classForm = reactive({
  id: 0,
  name: '',
  gradeId: undefined as number | undefined
})

// 学生列表相关
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const studentList = ref<any[]>([])
const currentClass = ref<any>(null)

const studentFormDialogVisible = ref(false)
const studentDialogTitle = ref('')
const isEditStudent = ref(false)
const studentSubmitting = ref(false)
const studentFormRef = ref<FormInstance>()

const studentForm = reactive({
  id: 0,
  studentNo: '',
  name: '',
  gender: 1,
  classId: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const studentRules: FormRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getGradeName = (gradeId: number) => {
  return gradeMap.value[gradeId] || '未知'
}

const loadGrades = async () => {
  try {
    const res = await request.get('/grade/list')
    gradeList.value = res.data || []
    gradeList.value.forEach(g => {
      gradeMap.value[g.id] = g.name
    })
  } catch (error) {
    console.error('加载年级失败', error)
  }
}

const loadClasses = async () => {
  loading.value = true
  try {
    const res = await request.get('/class/list')
    classList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载班级列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = async () => {
  isEdit.value = false
  dialogTitle.value = '添加班级'
  dialogVisible.value = true
  // 等待 DOM 更新（生成 el-option）
  await nextTick()
  const defaultGradeId = gradeList.value.length > 0 ? Number(gradeList.value[0].id) : undefined
  Object.assign(classForm, { id: 0, name: '', gradeId: defaultGradeId })
}

const showEditDialog = async (clazz: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑班级'
  dialogVisible.value = true
  // 等待 DOM 更新（生成 el-option）
  await nextTick()
  Object.assign(classForm, { id: clazz.id, name: clazz.name, gradeId: Number(clazz.gradeId) })
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/class/${classForm.id}`, classForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/class', classForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadClasses()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteClass = async (clazz: any) => {
  try {
    await ElMessageBox.confirm(`确定删除班级 "${clazz.name}"?`, '删除确认', {
      type: 'warning'
    })
    await request.delete(`/class/${clazz.id}`)
    ElMessage.success('删除成功')
    loadClasses()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 学生列表操作
const showStudents = async (clazz: any) => {
  currentClass.value = clazz
  studentDialogVisible.value = true
  loadStudents(clazz.id)
}

const loadStudents = async (classId: number) => {
  studentLoading.value = true
  try {
    const res = await request.get(`/student/class/${classId}`)
    studentList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学生列表失败')
  } finally {
    studentLoading.value = false
  }
}

const showAddStudentDialog = () => {
  isEditStudent.value = false
  studentDialogTitle.value = '添加学生'
  Object.assign(studentForm, {
    id: 0,
    studentNo: '',
    name: '',
    gender: 1,
    classId: currentClass.value?.id || 0,
    status: 1
  })
  studentFormDialogVisible.value = true
}

const showEditStudentDialog = (student: any) => {
  isEditStudent.value = true
  studentDialogTitle.value = '编辑学生'
  Object.assign(studentForm, {
    id: student.id,
    studentNo: student.studentNo,
    name: student.name,
    gender: student.gender,
    classId: student.classId,
    status: student.status
  })
  studentFormDialogVisible.value = true
}

const submitStudentForm = async () => {
  const valid = await studentFormRef.value?.validate()
  if (!valid) return

  studentSubmitting.value = true
  try {
    if (isEditStudent.value) {
      await request.put(`/student/${studentForm.id}`, studentForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/student', studentForm)
      ElMessage.success('创建成功')
    }
    studentFormDialogVisible.value = false
    loadStudents(currentClass.value?.id || 0)
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    studentSubmitting.value = false
  }
}

const deleteStudent = async (student: any) => {
  try {
    await ElMessageBox.confirm(`确定删除学生 "${student.name}"?`, '删除确认', {
      type: 'warning'
    })
    await request.delete(`/student/${student.id}`)
    ElMessage.success('删除成功')
    loadStudents(currentClass.value?.id || 0)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadGrades()
  loadClasses()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-dialog-header {
  margin-bottom: 16px;
}
</style>