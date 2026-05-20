/**
 * 注册页面
 */
const authAPI = require('../../services/auth')
const app = getApp()

Page({
  data: {
    username: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    email: '',
    loading: false
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  onEmailInput(e) {
    this.setData({ email: e.detail.value })
  },

  async handleRegister() {
    const { username, nickname, password, confirmPassword, email } = this.data

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (username.length < 3 || username.length > 20) {
      wx.showToast({ title: '用户名需3-20位', icon: 'none' })
      return
    }
    if (!nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' })
      return
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    if (this.data.loading) return
    this.setData({ loading: true })

    try {
      const res = await authAPI.register({
        username: username.trim(),
        nickname: nickname.trim(),
        password,
        email: email.trim() || undefined
      })

      // 自动登录
      const loginRes = await authAPI.login({
        username: username.trim(),
        password
      })
      app.setTokens(loginRes.accessToken, loginRes.refreshToken)
      wx.setStorageSync('user_info', res)

      wx.showToast({ title: '注册成功', icon: 'success' })

      setTimeout(() => {
        wx.navigateBack()
      }, 1000)
    } catch (e) {
      // 错误已由 request 处理
    } finally {
      this.setData({ loading: false })
    }
  },

  goToLogin() {
    wx.navigateBack()
  }
})
