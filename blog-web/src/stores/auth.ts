import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  login as apiLogin,
  register as apiRegister,
  refreshToken as apiRefreshToken,
  logout as apiLogout,
  getCurrentUser,
  type LoginDTO,
  type RegisterDTO,
  type UserInfo,
} from '@/api'
import { storage } from '@/utils/storage'
import { showSuccessMessage } from '@/utils/error'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
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
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      token.value = accessToken
      refreshToken.value = newRefreshToken
      storage.setAccessToken(accessToken)
      storage.setRefreshToken(newRefreshToken)
      // 获取用户信息
      await fetchCurrentUser()
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

  /**
   * 刷新 Access Token
   */
  async function refreshAccessToken(): Promise<boolean> {
    const rt = refreshToken.value || storage.getRefreshToken()
    if (!rt) return false

    try {
      const res = await apiRefreshToken({ refreshToken: rt })
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      token.value = accessToken
      refreshToken.value = newRefreshToken
      storage.setAccessToken(accessToken)
      storage.setRefreshToken(newRefreshToken)
      return true
    } catch {
      // 刷新失败，清除登录态
      clearAuth()
      return false
    }
  }

  async function logout(): Promise<void> {
    try {
      await apiLogout()
    } catch {
      // 忽略登出 API 错误
    }
    clearAuth()
  }

  function clearAuth(): void {
    token.value = null
    refreshToken.value = null
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
    const savedToken = storage.getAccessToken()
    const savedRefreshToken = storage.getRefreshToken()
    const savedUserInfo = storage.getUserInfo<UserInfo>()
    if (savedToken) {
      token.value = savedToken
    }
    if (savedRefreshToken) {
      refreshToken.value = savedRefreshToken
    }
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }
  }

  return {
    // State
    token,
    refreshToken,
    userInfo,
    // Getters
    isLoggedIn,
    isAdmin,
    nickname,
    avatar,
    // Actions
    login,
    register,
    refreshAccessToken,
    logout,
    clearAuth,
    fetchCurrentUser,
    initAuth,
  }
})