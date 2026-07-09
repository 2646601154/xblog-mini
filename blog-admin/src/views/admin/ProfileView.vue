<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { updateProfile, changePassword } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

// ========== 个人资料 ==========
const profileFormRef = ref<FormInstance>()
const profileSubmitting = ref(false)
const profileForm = ref({
  nickname: '',
  email: '',
  avatar: '',
})
const profileRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称需2-50字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

function initProfileForm() {
  profileForm.value = {
    nickname: authStore.userInfo?.nickname || '',
    email: authStore.userInfo?.email || '',
    avatar: authStore.userInfo?.avatar || '',
  }
}

async function handleProfileSubmit() {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    profileSubmitting.value = true
    try {
      await updateProfile(profileForm.value)
      ElMessage.success('资料更新成功')
      // 刷新本地 userInfo，保证 Header 头像/昵称同步
      await authStore.fetchCurrentUser()
      initProfileForm()
    } catch {
      // 错误提示由响应拦截器统一处理
    } finally {
      profileSubmitting.value = false
    }
  })
}

function handleProfileReset() {
  initProfileForm()
  profileFormRef.value?.clearValidate()
}

// ========== 修改密码 ==========
const passwordFormRef = ref<FormInstance>()
const passwordSubmitting = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码最少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (e?: Error) => void) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handlePasswordSubmit() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    passwordSubmitting.value = true
    try {
      await changePassword({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword,
      })
      ElMessage.success('密码修改成功')
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      passwordFormRef.value?.clearValidate()
    } catch {
      // 错误提示由响应拦截器统一处理
    } finally {
      passwordSubmitting.value = false
    }
  })
}

function handlePasswordReset() {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordFormRef.value?.clearValidate()
}

onMounted(() => {
  initProfileForm()
})
</script>

<template>
  <div class="max-w-2xl mx-auto space-y-6">
    <h2 class="text-2xl font-bold">个人中心</h2>

    <!-- 个人资料 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-medium">个人资料</span>
      </template>
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="80px"
      >
        <el-form-item label="用户名">
          <el-input :model-value="authStore.userInfo?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="authStore.userInfo?.role === 'admin' ? 'danger' : 'info'" size="small">
            {{ authStore.userInfo?.role === 'admin' ? '管理员' : '用户' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="2-50字符" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input v-model="profileForm.avatar" placeholder="头像URL" />
        </el-form-item>
        <el-form-item>
          <div class="flex gap-2">
            <el-button type="primary" :loading="profileSubmitting" @click="handleProfileSubmit">
              保存
            </el-button>
            <el-button @click="handleProfileReset">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card shadow="never">
      <template #header>
        <span class="font-medium">修改密码</span>
      </template>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="至少6位"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
          />
        </el-form-item>
        <el-form-item>
          <div class="flex gap-2">
            <el-button type="primary" :loading="passwordSubmitting" @click="handlePasswordSubmit">
              修改密码
            </el-button>
            <el-button @click="handlePasswordReset">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
