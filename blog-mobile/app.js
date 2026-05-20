/**
 * 小程序入口文件
 */
App({
  globalData: {
    userInfo: null,
    accessToken: null,
    refreshToken: null,
    baseUrl: 'https://api.xblog.com/api/v1'  // TODO: 替换为实际域名
  },

  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    const accessToken = wx.getStorageSync('access_token')
    const refreshToken = wx.getStorageSync('refresh_token')

    if (accessToken) {
      this.globalData.accessToken = accessToken
      this.globalData.refreshToken = refreshToken
    }
  },

  setTokens(accessToken, refreshToken) {
    this.globalData.accessToken = accessToken
    this.globalData.refreshToken = refreshToken
    wx.setStorageSync('access_token', accessToken)
    wx.setStorageSync('refresh_token', refreshToken)
  },

  clearTokens() {
    this.globalData.accessToken = null
    this.globalData.refreshToken = null
    wx.removeStorageSync('access_token')
    wx.removeStorageSync('refresh_token')
  }
})
