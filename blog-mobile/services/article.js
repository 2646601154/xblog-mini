/**
 * 文章相关 API
 */
const { get, post } = require('../utils/request')

module.exports = {
  /**
   * 获取文章列表
   * @param {Object} params - { page, size, categoryId, tagId }
   */
  getList(params) {
    return get('/articles', params)
  },

  /**
   * 获取文章详情
   * @param {number} id - 文章ID
   */
  getDetail(id) {
    return get(`/articles/${id}`)
  },

  /**
   * 获取文章评论列表
   * @param {number} articleId - 文章ID
   * @param {Object} params - { page, size }
   */
  getComments(articleId, params) {
    return get(`/articles/${articleId}/comments`, params)
  },

  /**
   * 获取文章上下篇
   * @param {number} id - 文章ID
   */
  getPrevNext(id) {
    return get(`/articles/${id}/prev-next`)
  }
}
