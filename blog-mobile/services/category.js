/**
 * 分类和标签 API
 */
const { get } = require('../utils/request')

module.exports = {
  /**
   * 获取全部分类
   */
  getCategories() {
    return get('/categories')
  },

  /**
   * 获取全部标签
   */
  getTags() {
    return get('/tags')
  }
}
