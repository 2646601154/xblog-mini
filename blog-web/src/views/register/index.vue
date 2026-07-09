<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/modules/auth'
import { storage } from '@/utils/storage'
import { useAuthStore } from '@/stores/auth'

interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  nickname: string
  email: string
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
})

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (value !== registerForm.password) {
    callback(new Error('确认密码与密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名3-20字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称2-20字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      email: registerForm.email || undefined,
    })

    ElMessage.success('注册成功，正在登录...')

    const success = await authStore.login({
      username: registerForm.username,
      password: registerForm.password,
    })

    if (success) {
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    }
  } catch (error) {
    const err = error as Error
    ElMessage.error(err.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push({
    path: '/login',
    query: route.query.redirect ? { redirect: route.query.redirect as string } : {},
  })
}

onMounted(() => {
  loadRemember()
})

function loadRemember() {
  const remember = storage.getRemember()
  if (remember) {
    registerForm.username = remember.username || ''
    registerForm.password = remember.password || ''
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <h1 class="register-title">注册</h1>
        <p class="register-subtitle">创建你的账号</p>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          label-position="top"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              class="pill-input"
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称"
              size="large"
              prefix-icon="UserFilled"
              class="pill-input"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱 (选填)"
              size="large"
              prefix-icon="Message"
              class="pill-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              class="pill-input"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
              class="pill-input"
            />
          </el-form-item>

          <el-form-item class="submit-item">
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: var(--space-xl);
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-card {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-3xl);
}

.register-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  text-align: center;
  margin: 0 0 var(--space-xs) 0;
  color: var(--text-primary);
}

.register-subtitle {
  font-size: var(--text-sm);
  text-align: center;
  margin: 0 0 var(--space-2xl) 0;
  color: var(--text-muted);
}

.register-form {
  margin-bottom: 0;
}

.register-form :deep(.el-form-item__error) {
  font-size: var(--text-xs);
  margin-top: var(--space-xs);
}

.register-form :deep(.el-form-item) {
  margin-bottom: var(--space-lg);
}

.pill-input {
  height: 48px;
}

.pill-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  height: 48px;
}

.submit-item :deep(.el-form-item__content) {
  justify-content: center;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: 600;
  border-radius: var(--radius-sm);
}

.register-footer {
  text-align: center;
  margin-top: var(--space-xl);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.register-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-card {
    padding: var(--space-2xl);
  }
}
</style>
