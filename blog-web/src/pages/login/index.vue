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
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
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
    <div class="login-bg"></div>
    <div class="login-container">
      <div class="login-card">
        <h1 class="login-title">登录</h1>

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
            />
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          </el-form-item>

          <el-form-item>
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
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1535747761923-a537e9c4b2d6?w=1920&q=80') no-repeat center center;
  background-size: cover;
  filter: blur(4px);
  transform: scale(1.05);
}

.login-bg::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.1));
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 0 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin: 0 0 32px 0;
  color: var(--text-primary);
}

.login-form {
  margin-bottom: 0;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
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
    padding: 32px 24px;
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
