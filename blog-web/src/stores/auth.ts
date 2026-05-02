import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, getCurrentUser, type LoginDTO, type RegisterDTO, type UserInfo } from '@/api'
import { storage } from '@/utils/storage'
import { showSuccessMessage } from '@/utils/error'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(null)
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // Actions
  async function login(credentials: LoginDTO): Promise<boolean> {
    try {
      const res = await apiLogin(credentials)
      const { token: newToken, user } = res.data
      token.value = newToken
      userInfo.value = user
      storage.setToken(newToken)
      storage.setUserInfo(user)
      showSuccessMessage('登录成功')
      return true
    } catch {
      return false
    }
  }

  async function register(data: RegisterDTO): Promise<boolean> {
    try {
      await apiRegister(data)
      showSuccessMessage('注册成功，请登录')
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
      userInfo.value = res.data
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
    // State
    token,
    userInfo,
    // Getters
    isLoggedIn,
    isAdmin,
    nickname,
    avatar,
    // Actions
    login,
    register,
    logout,
    fetchCurrentUser,
    initAuth,
  }
})
