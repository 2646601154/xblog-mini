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
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 1001 || res.data.code === 1002) {
            // Token 过期，尝试刷新
            const refreshed = await handleTokenRefresh()
            if (refreshed) {
              // 重试原请求
              const retryRes = await request(options)
              resolve(retryRes)
            } else {
              // 刷新失败，清除登录状态
              app.clearTokens()
              wx.navigateTo({ url: '/pages/user/login' })
              reject(res.data)
            }
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // 未登录
          app.clearTokens()
          wx.navigateTo({ url: '/pages/user/login' })
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

    if (res.data.code === 200) {
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
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
