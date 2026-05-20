/**
 * 登录页面
 */
const authAPI = require('../../services/auth')
const app = getApp()

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  async handleLogin() {
    const { username, password } = this.data

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    if (this.data.loading) return
    this.setData({ loading: true })

    try {
      const res = await authAPI.login({ username, password })

      // 保存 Token
      app.setTokens(res.accessToken, res.refreshToken)

      // 获取用户信息
      const userInfo = await authAPI.getMe()
      wx.setStorageSync('user_info', userInfo)

      wx.showToast({ title: '登录成功', icon: 'success' })

      // 返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1000)
    } catch (e) {
      // 错误已由 request 处理
    } finally {
      this.setData({ loading: false })
    }
  },

  handleWechatLogin() {
    // 微信一键登录
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: async (res) => {
        const { userInfo } = res
        // TODO: 调用微信登录接口
        console.log('微信用户信息:', userInfo)
      },
      fail: (e) => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/user/register' })
  }
})
