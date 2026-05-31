<template>
  <div class="login-page">
    <LoginBackground />

    <div class="login-content">
      <!-- 品牌标题 -->
      <div class="brand-header">
        <div class="brand-icon">
          <svg viewBox="0 0 40 40" width="48" height="48">
            <defs>
              <linearGradient id="iconGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#00d4ff" />
                <stop offset="100%" stop-color="#8b5cf6" />
              </linearGradient>
            </defs>
            <rect x="2" y="2" width="36" height="36" rx="8" fill="none" stroke="url(#iconGrad)" stroke-width="2"/>
            <path d="M12 14h16M12 20h16M12 26h10" stroke="url(#iconGrad)" stroke-width="2" stroke-linecap="round"/>
            <circle cx="28" cy="26" r="4" fill="url(#iconGrad)" opacity="0.8"/>
          </svg>
        </div>
        <h1 class="brand-title">智教云台</h1>
        <p class="brand-subtitle">智能教学管理平台 · AI驱动</p>
      </div>

      <!-- 卡片容器 -->
      <div class="card-container">
        <Transition name="form-switch" mode="out-in">
          <div class="login-card" v-if="!isRegister" key="login">
            <div class="card-header">
              <h2>欢迎登录</h2>
              <p class="card-desc">登录以继续访问您的教学空间</p>
            </div>

            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="rules"
              label-width="0"
              class="login-form"
            >
              <el-form-item prop="username">
                <div class="input-wrapper">
                  <span class="input-icon">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                  </span>
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入用户名"
                    size="large"
                    class="tech-input"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-wrapper">
                  <span class="input-icon">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                  </span>
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    class="tech-input"
                    show-password
                  />
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="loading"
                  @click="handleLogin"
                  class="login-btn"
                  style="width: 100%"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>

            <div class="card-footer">
              <span>还没有账号？</span>
              <el-button text class="switch-btn" @click="isRegister = true">立即注册</el-button>
            </div>
          </div>

          <!-- 注册卡片 -->
          <div class="login-card" v-else key="register">
            <div class="card-header">
              <h2>创建账号</h2>
              <p class="card-desc">注册新账号以使用教学系统</p>
            </div>

            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              label-width="0"
              class="login-form"
            >
              <el-form-item prop="username">
                <div class="input-wrapper">
                  <span class="input-icon">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                  </span>
                  <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    size="large"
                    class="tech-input"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-wrapper">
                  <span class="input-icon">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                  </span>
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请输入密码（至少6位）"
                    size="large"
                    class="tech-input"
                    show-password
                  />
                </div>
              </el-form-item>

              <el-form-item prop="realName">
                <div class="input-wrapper">
                  <span class="input-icon">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                      <circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
                    </svg>
                  </span>
                  <el-input
                    v-model="registerForm.realName"
                    placeholder="请输入真实姓名"
                    size="large"
                    class="tech-input"
                  />
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="regLoading"
                  @click="handleRegister"
                  class="login-btn"
                  style="width: 100%"
                >
                  注册
                </el-button>
              </el-form-item>
            </el-form>

            <div class="card-footer">
              <span>已有账号？</span>
              <el-button text class="switch-btn" @click="isRegister = false">返回登录</el-button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { register } from '@/api/user'
import LoginBackground from './LoginBackground.vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const regLoading = ref(false)
const isRegister = ref(false)

// 抖动输入框：找到验证失败的表单项，抖动它们
const shakeInputs = () => {
  setTimeout(() => {
    const items = document.querySelectorAll('.el-form-item.is-error')
    if (items.length === 0) return

    items.forEach((item) => {
      const el = item as HTMLElement
      const offsets = [-8, 8, -8, 8, -6, 6, -4, 4, -2, 2, 0]
      let i = 0

      const step = () => {
        if (i >= offsets.length) {
          el.style.marginLeft = ''
          return
        }
        el.style.marginLeft = `${offsets[i]}px`
        i++
        setTimeout(step, 40)
      }

      el.style.marginLeft = ''
      setTimeout(step, 10)
    })
  }, 200)
}

const handleLogin = () => {
  loginFormRef.value?.validate().then(() => {
    loading.value = true
    userStore.loginAction(loginForm.username, loginForm.password)
      .then(() => {
        ElMessage.success('登录成功')
        router.push('/dashboard')
      })
      .catch((error: any) => {
        console.error(error)
      })
      .finally(() => {
        loading.value = false
      })
  }).catch(() => {
    shakeInputs()
  })
}

const handleRegister = () => {
  registerFormRef.value?.validate().then(() => {
    regLoading.value = true
    register(registerForm)
      .then(() => {
        ElMessage.success('注册成功，请登录')
        isRegister.value = false
        loginForm.username = registerForm.username
        registerForm.username = ''
        registerForm.password = ''
        registerForm.realName = ''
      })
      .catch((error: any) => {
        console.error(error)
      })
      .finally(() => {
        regLoading.value = false
      })
  }).catch(() => {
    shakeInputs()
  })
}

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
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary, #1E40AF) 0%, var(--color-primary-light, #3B82F6) 50%, var(--color-primary-lighter, #60A5FA) 100%);
}

.login-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: contentFadeIn 0.8s ease-out forwards;
}

/* 品牌标题 */
.brand-header {
  text-align: center;
  margin-bottom: 30px;
  animation: slideDown 0.6s ease-out forwards;
}

.brand-icon {
  margin-bottom: 16px;
  filter: drop-shadow(0 0 20px rgba(0, 212, 255, 0.4));
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #00d4ff, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  letter-spacing: 4px;
}

[data-theme="light"] .brand-subtitle {
  color: rgba(255, 255, 255, 0.6);
}

/* 卡片容器 */
.card-container {
  width: 420px;
  min-height: 460px;
  position: relative;
}

/* 毛玻璃卡片 */
.login-card {
  width: 420px;
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.05) inset,
    0 0 80px rgba(0, 212, 255, 0.05);
}

[data-theme="light"] .login-card {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset,
    0 0 80px rgba(0, 212, 255, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 22px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 8px 0;
  font-weight: 600;
}

[data-theme="light"] .card-header h2 {
  color: rgba(255, 255, 255, 0.98);
}

.card-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0;
}

[data-theme="light"] .card-desc {
  color: rgba(255, 255, 255, 0.55);
}

/* 表单 */
.login-form {
  margin-top: 8px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  color: rgba(0, 212, 255, 0.6);
  display: flex;
  align-items: center;
}

[data-theme="light"] .input-icon {
  color: rgba(0, 212, 255, 0.7);
}

/* 深度选择器修改 Element Plus 输入框样式 */
.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding-left: 42px !important;
  transition: all 0.3s ease !important;
}

[data-theme="light"] .login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.18) !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.3) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(0, 212, 255, 0.6) !important;
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1), 0 0 20px rgba(0, 212, 255, 0.1) !important;
}

.login-form :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 14px;
}

[data-theme="light"] .login-form :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.95) !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

.login-form :deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.4) !important;
}

/* 登录按钮 */
.login-btn {
  background: linear-gradient(135deg, #00d4ff, #8b5cf6) !important;
  border: none !important;
  border-radius: 10px !important;
  height: 44px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  letter-spacing: 2px;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.2) !important;
}

.login-btn:hover {
  filter: brightness(1.15) !important;
  box-shadow: 0 6px 25px rgba(0, 212, 255, 0.35) !important;
  transform: translateY(-1px);
}

[data-theme="light"] .login-btn {
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.25) !important;
}

[data-theme="light"] .login-btn:hover {
  box-shadow: 0 6px 25px rgba(0, 212, 255, 0.4) !important;
}

.login-btn:active {
  transform: scale(0.98);
}

/* 卡片底部切换 */
.card-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

[data-theme="light"] .card-footer {
  color: rgba(255, 255, 255, 0.55);
}

.switch-btn {
  color: #00d4ff !important;
  font-size: 13px !important;
  padding: 0 4px !important;
  transition: all 0.3s ease !important;
}

.switch-btn:hover {
  color: #8b5cf6 !important;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}

[data-theme="light"] .switch-btn {
  color: #00d4ff !important;
}

[data-theme="light"] .switch-btn:hover {
  color: #8b5cf6 !important;
  text-shadow: 0 0 12px rgba(0, 212, 255, 0.4);
}

/* 表单切换动画 */
.form-switch-enter-active {
  transition: all 0.35s ease-out;
}

.form-switch-leave-active {
  transition: all 0.25s ease-in;
}

.form-switch-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.form-switch-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 动画关键帧 */
@keyframes contentFadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}

@keyframes slideDown {
  0% {
    opacity: 0;
    transform: translateY(-30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 移动端适配 ===== */
@media (max-width: 768px) {
  .login-page {
    padding: 12px;
    align-items: flex-start;
    justify-content: center;
  }

  .login-content {
    width: 100% !important;
    max-width: 100% !important;
    padding: 0;
    justify-content: center;
  }

  .brand-header {
    margin-bottom: 16px;
  }

  .brand-title {
    font-size: 26px !important;
  }

  .brand-subtitle {
    font-size: 12px !important;
    letter-spacing: 2px !important;
  }

  .brand-icon {
    margin-bottom: 10px;
  }

  .brand-icon svg {
    width: 40px;
    height: 40px;
  }

  .card-container {
    width: 100% !important;
    max-width: 100% !important;
    min-height: auto;
  }

  .login-card {
    width: 100% !important;
    padding: 24px 20px !important;
    border-radius: 12px !important;
    box-sizing: border-box;
  }

  .card-header {
    margin-bottom: 24px;
  }

  .card-header h2 {
    font-size: 20px !important;
  }

  .card-desc {
    font-size: 12px !important;
  }

  /* 表单间距紧凑 */
  .login-form :deep(.el-form-item) {
    margin-bottom: 16px !important;
  }

  .login-form :deep(.el-input__wrapper) {
    padding-left: 38px !important;
    border-radius: 8px !important;
  }

  .input-icon {
    left: 10px;
  }

  .input-icon svg {
    width: 16px;
    height: 16px;
  }

  .login-btn {
    height: 42px !important;
    font-size: 14px !important;
  }

  .card-footer {
    margin-top: 16px;
  }

  /* 隐藏网格背景节省性能 */
  .grid-overlay {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 8px;
  }

  .login-card {
    padding: 20px 16px !important;
    border-radius: 10px !important;
  }

  .brand-title {
    font-size: 22px !important;
  }

  .brand-subtitle {
    font-size: 11px !important;
    letter-spacing: 1px !important;
  }

  .card-header h2 {
    font-size: 18px !important;
  }

  .login-form :deep(.el-form-item) {
    margin-bottom: 14px !important;
  }

  .card-footer {
    font-size: 12px;
    margin-top: 14px;
  }

  .switch-btn {
    font-size: 12px !important;
  }
}

</style>