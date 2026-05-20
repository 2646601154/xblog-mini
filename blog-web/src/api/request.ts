import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { storage } from '@/utils/storage'
import { useAuthStore } from '@/stores/auth'
import { showErrorMessage } from '@/utils/error'

const baseURL = import.meta.env.VITE_API_BASE_URL

// 创建 Axios 实例
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

// 订阅刷新完成事件
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者新 Token（或刷新失败
function onTokenRefreshed(newToken: string) {
  refreshSubscribers.forEach((callback) => callback(newToken))
  refreshSubscribers = []
}

// 刷新 Access Token
async function doRefreshToken(): Promise<string | null> {
  const refreshToken = storage.getRefreshToken()
  if (!refreshToken) return null

  try {
    const res = await axios.post(`${baseURL}/api/v1/auth/refresh`, {
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

// 公开端点：这些 URL 不需要登录，401 时不应跳转登录页
function isPublicEndpoint(url: string | undefined): boolean {
  if (!url) return false
  const publicPatterns = ['/v1/articles', '/v1/categories', '/v1/tags', '/v1/comments', '/v1/configs']
  return publicPatterns.some(pattern => url.startsWith(pattern))
}

// 请求拦截器：自动带上 Authorization
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
  }
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data

    // 业务错误处理
    if (code !== 200) {
      // 401 清理登录态并跳转登录页
      if (code === 1001 || code === 1002) {
        storage.clearAuth()
        showErrorMessage(code)
        if (!isPublicEndpoint(response.config.url)) {
          window.location.href = '/login'
        }
        return Promise.reject(new Error(message))
      }

      showErrorMessage(code, message)
      return Promise.reject(new Error(message))
    }

    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // 网络错误处理
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
      return Promise.reject(error)
    }

    if (error.response) {
      const status = error.response.status

      // 401 未授权：尝试刷新 Token
      if (status === 401 && !originalRequest._retry) {
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

        // 刷新失败或没有 refreshToken，清除登录态并跳转
        storage.clearAuth()
        showErrorMessage(1001)
        if (!isPublicEndpoint(originalRequest.url)) {
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }

      // 其他 HTTP 错误
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
  }
)

export default request