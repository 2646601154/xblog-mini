/**
 * 统一请求封装
 * 支持 Token 自动刷新、错误处理
 */

// Token 刷新队列
let isRefreshing = false
let refreshSubscribers = []

// 订阅 Token 刷新
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者 Token 已刷新
const notifyTokenRefresh = (newToken) => {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

// 公开接口列表 - 这些接口401时不应跳转登录页
const PUBLIC_ENDPOINTS = ['/articles', '/categories', '/tags', '/comments']

function isPublicEndpoint(url) {
  if (!url) return false
  return PUBLIC_ENDPOINTS.some(pattern => url.includes(pattern))
}

/**
 * 基础请求方法
 */
const request = (options) => {
  const app = getApp()
  const baseUrl = app.globalData.baseUrl

  return new Promise((resolve, reject) => {
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }

    // 添加 Token
    const accessToken = app.globalData.accessToken
    if (accessToken && !options.noAuth) {
      header['Authorization'] = `Bearer ${accessToken}`
    }

    wx.request({
      url: `${baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: async (res) => {
        // 确保 res.data 是对象（小程序某些情况下返回字符串）
        let responseData = res.data
        if (typeof responseData === 'string') {
          try {
            responseData = JSON.parse(responseData)
          } catch (e) {
            wx.showToast({ title: '响应格式错误', icon: 'none' })
            reject({ message: '响应格式错误' })
            return
          }
        }

        if (res.statusCode === 200) {
          if (responseData.code === 200) {
            resolve(responseData.data)
          } else if (responseData.code === 1001 || responseData.code === 1002) {
            // Token 过期，尝试刷新
            const refreshed = await handleTokenRefresh()
            if (refreshed) {
              // 重试原请求
              const retryRes = await request(options)
              resolve(retryRes)
            } else {
              // 刷新失败，清除登录状态
              app.clearTokens()
              if (!isPublicEndpoint(options.url)) {
                wx.navigateTo({ url: '/pages/user/login' })
              } else {
                // 公开接口 token 无效时，返回空数据而非 reject
                resolve(null)
                return
              }
              reject(responseData)
            }
          } else {
            wx.showToast({
              title: responseData.message || '请求失败',
              icon: 'none'
            })
            reject(responseData)
          }
        } else if (res.statusCode === 401) {
          // 未登录
          app.clearTokens()
          if (!isPublicEndpoint(options.url)) {
            wx.navigateTo({ url: '/pages/user/login' })
          } else {
            // 公开接口 401 时，返回空数据而非 reject
            resolve(null)
            return
          }
          reject({ message: '请先登录' })
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject({ message: '网络错误' })
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * 处理 Token 刷新
 */
const handleTokenRefresh = async () => {
  if (isRefreshing) {
    // 等待刷新完成
    return new Promise((resolve) => {
      subscribeTokenRefresh((newToken) => {
        resolve(newToken)
      })
    })
  }

  isRefreshing = true
  const app = getApp()
  const refreshToken = app.globalData.refreshToken

  if (!refreshToken) {
    isRefreshing = false
    return false
  }

  try {
    const res = await wx.request({
      url: `${app.globalData.baseUrl}/auth/refresh`,
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { refreshToken }
    })

    // 确保 res.data 是对象
    let refreshData = res.data
    if (typeof refreshData === 'string') {
      refreshData = JSON.parse(refreshData)
    }

    if (refreshData.code === 200) {
      const { accessToken, refreshToken: newRefreshToken } = refreshData.data
      app.setTokens(accessToken, newRefreshToken)
      notifyTokenRefresh(accessToken)
      isRefreshing = false
      return true
    } else {
      isRefreshing = false
      return false
    }
  } catch (e) {
    isRefreshing = false
    return false
  }
}

// 便捷方法
const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options })
}

const post = (url, data, options = {}) => {
  return request({ url, method: 'POST', data, ...options })
}

const put = (url, data, options = {}) => {
  return request({ url, method: 'PUT', data, ...options })
}

const del = (url, data, options = {}) => {
  return request({ url, method: 'DELETE', data, ...options })
}

module.exports = {
  request,
  get,
  post,
  put,
  del
}
