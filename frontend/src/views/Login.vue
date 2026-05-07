<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>教师智能教学系统</h2>
        </div>
      </template>

      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-button text @click="showRegister = true">注册账号</el-button>
      </div>
    </el-card>

    <!-- 注册对话框 -->
    <el-dialog v-model="showRegister" title="注册账号" width="400px">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { register } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const showRegister = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerForm = reactive({
  username: '',
  password: '',
  realName: ''
})

const registerRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await userStore.loginAction(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate()
  if (!valid) return

  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    loginForm.username = registerForm.username
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  color: #303133;
  margin: 0;
}

.login-footer {
  text-align: center;
}
</style>