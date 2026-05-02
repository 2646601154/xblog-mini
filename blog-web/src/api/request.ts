import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { storage } from '@/utils/storage'
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

// 请求拦截器：自动带上 Authorization
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = storage.getToken()
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
        window.location.href = '/login'
        return Promise.reject(new Error(message))
      }

      showErrorMessage(code, message)
      return Promise.reject(new Error(message))
    }

    return response
  },
  (error: AxiosError) => {
    // 网络错误处理
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.response) {
      // HTTP 错误状态码
      const status = error.response.status
      switch (status) {
        case 401:
          storage.clearAuth()
          showErrorMessage(1001)
          window.location.href = '/login'
          break
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
