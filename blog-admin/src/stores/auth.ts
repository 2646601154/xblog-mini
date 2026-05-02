import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, getCurrentUser, type LoginDTO, type UserInfo } from '@/api'
import { storage } from '@/utils/storage'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  async function login(credentials: LoginDTO): Promise<boolean> {
    try {
      const res = await apiLogin(credentials)
      const { token: newToken, user } = res.data.data
      token.value = newToken
      userInfo.value = user
      storage.setToken(newToken)
      storage.setUserInfo(user)
      ElMessage.success('登录成功')
      return true
    } catch {
      return false
    }
  }

  function logout(): void {
    token.value = null
    userInfo.value = null
    storage.clearAuth()
  }

  async function fetchCurrentUser(): Promise<boolean> {
    if (!token.value) return false
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data.data
      if (userInfo.value) {
        storage.setUserInfo(userInfo.value)
      }
      return true
    } catch {
      return false
    }
  }

  function initAuth(): void {
    const savedToken = storage.getToken()
    const savedUserInfo = storage.getUserInfo<UserInfo>()
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    nickname,
    avatar,
    login,
    logout,
    fetchCurrentUser,
    initAuth,
  }
})