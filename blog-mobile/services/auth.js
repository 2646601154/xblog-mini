/**
 * 认证相关 API
 */
const { get, post } = require('../utils/request')

module.exports = {
  /**
   * 登录
   * @param {Object} data - { username, password }
   */
  login(data) {
    return post('/auth/login', data)
  },

  /**
   * 注册
   * @param {Object} data - { username, password, nickname, email? }
   */
  register(data) {
    return post('/auth/register', data)
  },

  /**
   * 获取当前用户信息
   */
  getMe() {
    return get('/auth/me')
  },

  /**
   * 登出
   */
  logout() {
    return post('/auth/logout')
  },

  /**
   * 微信登录（需要后端配合）
   * @param {Object} data - { code, nickname?, avatar? }
   */
  wechatLogin(data) {
    return post('/auth/wechat-login', data)
  }
}
