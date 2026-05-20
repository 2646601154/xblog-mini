/**
 * 用户页面
 */
const authAPI = require('../../services/auth')
const app = getApp()

Page({
  data: {
    userInfo: null,
    isLoggedIn: false,
    menuItems: [
      { id: 'comments', icon: '💬', title: '我的评论', url: '/pages/user/comments' },
      { id: 'favorites', icon: '⭐', title: '我的收藏', url: '/pages/user/favorites' },
      { id: 'settings', icon: '⚙️', title: '设置', url: '/pages/user/settings' }
    ]
  },

  onLoad() {
    this.checkLogin()
  },

  onShow() {
    this.checkLogin()
  },

  checkLogin() {
    const token = app.globalData.accessToken
    const userInfo = wx.getStorageSync('user_info')
    this.setData({
      isLoggedIn: !!token,
      userInfo: userInfo || null
    })
  },

  goToLogin() {
    wx.navigateTo({ url: '/pages/user/login' })
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/user/register' })
  },

  async handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await authAPI.logout()
          } catch (e) {
            // 忽略错误
          }
          app.clearTokens()
          wx.removeStorageSync('user_info')
          this.setData({ isLoggedIn: false, userInfo: null })
          wx.showToast({ title: '已退出登录', icon: 'success' })
        }
      }
    })
  },

  goToMenu(e) {
    const { url } = e.currentTarget.dataset
    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    wx.navigateTo({ url })
  },

  onShareAppMessage() {
    return {
      title: '欢迎访问博客',
      path: '/pages/index/index'
    }
  }
})
