<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { storage } from '@/utils/storage'

interface LoginForm {
  username: string
  password: string
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名3-20字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6字符', trigger: 'blur' },
  ],
}

function loadRemember() {
  const remember = storage.getRemember()
  if (remember) {
    loginForm.username = remember.username || ''
    loginForm.password = remember.password || ''
    rememberMe.value = !!remember.username
  }
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
    })

    if (success) {
      if (rememberMe.value) {
        storage.setRemember(loginForm.username, loginForm.password)
      } else {
        storage.clearRemember()
      }

      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    }
  } catch (error) {
    const err = error as Error
    ElMessage.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  router.push({
    path: '/register',
    query: route.query.redirect ? { redirect: route.query.redirect as string } : {},
  })
}

onMounted(() => {
  loadRemember()
})
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <h1 class="login-title">登录</h1>
        <p class="login-subtitle">欢迎回来</p>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              class="pill-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              class="pill-input"
            />
          </el-form-item>

          <el-form-item class="remember-item">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          </el-form-item>

          <el-form-item class="submit-item">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">去注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: var(--space-xl);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-3xl);
}

.login-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  text-align: center;
  margin: 0 0 var(--space-xs) 0;
  color: var(--text-primary);
}

.login-subtitle {
  font-size: var(--text-sm);
  text-align: center;
  margin: 0 0 var(--space-2xl) 0;
  color: var(--text-muted);
}

.login-form {
  margin-bottom: 0;
}

.login-form :deep(.el-form-item__error) {
  font-size: var(--text-xs);
  margin-top: var(--space-xs);
}

.login-form :deep(.el-form-item) {
  margin-bottom: var(--space-lg);
}

.pill-input {
  height: 48px;
}

.pill-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-pill);
  height: 48px;
}

.remember-item :deep(.el-form-item__content) {
  justify-content: flex-start;
}

.submit-item :deep(.el-form-item__content) {
  justify-content: center;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: 600;
  border-radius: var(--radius-pill);
}

.login-footer {
  text-align: center;
  margin-top: var(--space-xl);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.login-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    padding: var(--space-2xl);
  }
}
</style>
