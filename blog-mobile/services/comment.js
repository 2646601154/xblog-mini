/**
 * 评论相关 API
 */
const { get, post, put, del } = require('../utils/request')

module.exports = {
  /**
   * 发表评论
   * @param {Object} data - { articleId, content }
   */
  publish(data) {
    return post('/comments', data)
  },

  /**
   * 获取我的评论
   * @param {Object} params - { page, size, status }
   */
  getMyComments(params) {
    return get('/comments/my', params)
  },

  /**
   * 编辑评论
   * @param {number} id - 评论ID
   * @param {Object} data - { content }
   */
  update(id, data) {
    return put(`/comments/${id}`, data)
  },

  /**
   * 删除评论
   * @param {number} id - 评论ID
   */
  delete(id) {
    return del(`/comments/${id}`)
  }
}
