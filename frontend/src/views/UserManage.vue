<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-for="role in userRoles[row.id]" :key="role" :type="getRoleType(role)" class="role-tag">
              {{ roleMap[role] || role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" link @click="showRoleDialog(row)">
              <el-icon><UserFilled /></el-icon>
              角色
            </el-button>
            <el-button type="danger" link @click="deleteUser(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="userForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" v-if="!isEdit">
          <el-select v-model="userForm.roleCode" placeholder="选择角色">
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" v-model="roleDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <span>{{ currentUser?.realName || currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="选择角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox value="ADMIN">管理员</el-checkbox>
            <el-checkbox value="TEACHER">教师</el-checkbox>
            <el-checkbox value="STUDENT">学生</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="assignRoles" :loading="assigning">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Edit, UserFilled, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const userList = ref<any[]>([])
const userRoles = ref<Record<number, string[]>>({})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const roleDialogVisible = ref(false)
const currentUser = ref<any>(null)
const selectedRoles = ref<string[]>([])
const assigning = ref(false)

const userForm = reactive({
  id: 0,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleCode: 'TEACHER',
  status: 1
})

const roleMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生'
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getRoleType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'primary'
    case 'STUDENT': return 'success'
    default: return 'info'
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list')
    userList.value = res.data || []
    // 加载每个用户的角色
    for (const user of userList.value) {
      try {
        const roleRes = await request.get(`/user/${user.id}/roles`)
        userRoles.value[user.id] = roleRes.data || []
      } catch (e) {
        userRoles.value[user.id] = []
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '添加用户'
  Object.assign(userForm, {
    id: 0,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    roleCode: 'TEACHER',
    status: 1
  })
  dialogVisible.value = true
}

const showEditDialog = (user: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    email: user.email,
    phone: user.phone,
    status: user.status
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/user/${userForm.id}`, userForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/user', userForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteUser = async (user: any) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${user.realName || user.username}"?`, '删除确认', {
      type: 'warning'
    })
    await request.delete(`/user/${user.id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const showRoleDialog = (user: any) => {
  currentUser.value = user
  selectedRoles.value = userRoles.value[user.id] || []
  roleDialogVisible.value = true
}

const assignRoles = async () => {
  if (!currentUser.value) return
  assigning.value = true
  try {
    // 逐个分配角色
    for (const role of selectedRoles.value) {
      await request.put(`/user/${currentUser.value.id}/role?roleCode=${role}`)
    }
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败')
  } finally {
    assigning.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-tag {
  margin-right: 4px;
}
</style>