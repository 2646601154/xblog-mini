/**
 * 分类页
 */
const categoryAPI = require('../../services/category')
const articleAPI = require('../../services/article')

Page({
  data: {
    categories: [],
    tags: [],
    selectedCategory: null,
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadCategories()
    this.loadTags()
  },

  async loadCategories() {
    try {
      const res = await categoryAPI.getCategories()
      this.setData({ categories: res })
    } catch (e) {
      // TODO: 使用示例数据
      this.setData({ categories: [] })
    }
  },

  async loadTags() {
    try {
      const res = await categoryAPI.getTags()
      this.setData({ tags: res })
    } catch (e) {
      this.setData({ tags: [] })
    }
  },

  selectCategory(e) {
    const { id } = e.currentTarget.dataset
    this.setData({
      selectedCategory: id,
      articles: [],
      page: 1,
      hasMore: true
    })
    if (id) {
      this.loadArticlesByCategory()
    }
  },

  async loadArticlesByCategory() {
    this.setData({ loading: true })
    try {
      const res = await articleAPI.getList({
        page: this.data.page,
        size: this.data.size,
        categoryId: this.data.selectedCategory
      })
      this.setData({
        articles: res.records,
        hasMore: res.records.length < res.total,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/article?id=${id}`
    })
  }
})
