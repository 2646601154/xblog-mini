import axios, {
  type AxiosInstance,
  type AxiosError,
  type InternalAxiosRequestConfig,
  type AxiosResponse,
} from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storage } from '@/utils/storage'
import { useAuthStore } from '@/stores/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 刷新 Token 的状态管理
let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

// 会话过期弹窗状态，防止重复弹出
let isSessionExpiredDialogShown = false

/**
 * 显示登录过期弹窗，让用户选择保持此页或跳转至登录
 */
function showSessionExpiredDialog(message: string = '登录已过期，请重新登录'): void {
  if (isSessionExpiredDialogShown) return

  // 如果当前已经在登录页，不弹出会话过期弹窗，避免用户在登录时看到"请重新登录"的困惑提示
  if (window.location.pathname === '/login') {
    storage.clearAuth()
    try {
      const authStore = useAuthStore()
      authStore.clearAuth()
    } catch {
      // 忽略
    }
    return
  }

  isSessionExpiredDialogShown = true

  ElMessageBox.confirm(message, '会话过期', {
    confirmButtonText: '跳转至登录',
    cancelButtonText: '保持此页',
    type: 'warning',
    closeOnClickModal: false,
    closeOnPressEscape: false,
    showClose: false,
  })
    .then(() => {
      // 用户选择跳转：清除登录态并跳转
      storage.clearAuth()
      try {
        const authStore = useAuthStore()
        authStore.clearAuth()
      } catch {
        // useAuthStore 在 Pinia 未初始化时可能报错，忽略
      }
      window.location.href = '/login'
    })
    .catch(() => {
      // 用户选择保持此页：仅清除内存态，保留 storage 以便后续恢复
      try {
        const authStore = useAuthStore()
        authStore.clearAuth()
      } catch {
        // 忽略
      }
    })
    .finally(() => {
      isSessionExpiredDialogShown = false
    })
}

function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

function onTokenRefreshed(newToken: string) {
  refreshSubscribers.forEach((callback) => callback(newToken))
  refreshSubscribers = []
}

// 刷新 Access Token
async function doRefreshToken(): Promise<string | null> {
  const refreshToken = storage.getRefreshToken()
  if (!refreshToken) return null

  try {
    const res = await request.post('/v1/auth/refresh', {
      refreshToken,
    })
    if (res.data.code === 200) {
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      storage.setAccessToken(accessToken)
      storage.setRefreshToken(newRefreshToken)

      // 同步更新 authStore
      const authStore = useAuthStore()
      authStore.token = accessToken
      authStore.refreshToken = newRefreshToken

      return accessToken
    }
  } catch (e) {
    console.error('刷新 Token 失败:', e)
  }
  return null
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = storage.getAccessToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)
/**
 * 响应拦截器：统一处理响应状态码和错误
 */
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message } = response.data

    if (code !== 200) {
      if (code === 1001 || code === 1002) {
        showSessionExpiredDialog(message || '登录已过期，请重新登录')
        return Promise.reject(new Error(message))
      }

      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }

    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    }

    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
      return Promise.reject(error)
    }

    if (error.response) {
      const status = error.response.status

      // 401 未授权：尝试刷新 Token
      if (status === 401 && !originalRequest._retry) {
        // 主动登出请求的 401 直接清除状态，不弹窗不刷新
        if (originalRequest.url?.includes('/auth/logout')) {
          storage.clearAuth()
          return Promise.reject(error)
        }

        originalRequest._retry = true

        if (isRefreshing) {
          // 正在刷新中，排队等待
          return new Promise((resolve, reject) => {
            subscribeTokenRefresh((newToken: string) => {
              if (newToken) {
                originalRequest.headers = originalRequest.headers || {}
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                resolve(request(originalRequest))
              } else {
                // 刷新失败，拒绝所有排队的请求
                reject(error)
              }
            })
          })
        }

        isRefreshing = true

        try {
          const newToken = await doRefreshToken()
          if (newToken) {
            onTokenRefreshed(newToken)
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return request(originalRequest)
          }
          // 刷新失败，newToken 为 null
          onTokenRefreshed('') // 通知所有订阅者刷新失败
        } catch (e) {
          console.error('Token 刷新异常:', e)
          onTokenRefreshed('') // 通知所有订阅者刷新失败
        } finally {
          isRefreshing = false
        }

        // 刷新失败或没有 refreshToken，显示过期弹窗
        showSessionExpiredDialog('登录已过期，请重新登录')
        return Promise.reject(error)
      }

      switch (status) {
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error('请求失败，请稍后重试')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }

    return Promise.reject(error)
  },
)

export default request
