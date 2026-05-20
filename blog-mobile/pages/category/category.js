/**
 * 分类页
 */
const categoryAPI = require('../../services/category')
const articleAPI = require('../../services/article')

Page({
  data: {
    categories: [],
    selectedCategory: null,
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadCategories()
  },

  async loadCategories() {
    try {
      const res = await categoryAPI.getCategories()
      this.setData({ categories: res })
    } catch (e) {
      this.setData({ categories: [] })
    }
  },

  selectCategory(e) {
    const { id } = e.currentTarget.dataset
    const newCategory = this.data.selectedCategory === id ? null : id
    this.setData({
      selectedCategory: newCategory,
      articles: [],
      page: 1,
      hasMore: true
    })
    if (newCategory) {
      this.loadArticlesByCategory()
    }
  },

  async loadArticlesByCategory(isLoadMore = false) {
    if (this.data.loading) return

    this.setData({ loading: true })
    try {
      const res = await articleAPI.getList({
        page: isLoadMore ? this.data.page : 1,
        size: this.data.size,
        categoryId: this.data.selectedCategory
      })

      const articles = isLoadMore
        ? [...this.data.articles, ...res.records]
        : res.records

      this.setData({
        articles,
        page: this.data.page + 1,
        hasMore: articles.length < res.total,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading && this.data.selectedCategory) {
      this.loadArticlesByCategory(true)
    }
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/article?id=${id}`
    })
  }
})
