/**
 * 标签页
 */
const categoryAPI = require('../../services/category')
const articleAPI = require('../../services/article')

Page({
  data: {
    tags: [],
    selectedTag: null,
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadTags()
  },

  async loadTags() {
    try {
      const res = await categoryAPI.getTags()
      this.setData({ tags: res })
    } catch (e) {
      this.setData({ tags: [] })
    }
  },

  selectTag(e) {
    const { id } = e.currentTarget.dataset
    const newTag = this.data.selectedTag === id ? null : id
    this.setData({
      selectedTag: newTag,
      articles: [],
      page: 1,
      hasMore: true
    })
    if (newTag) {
      this.loadArticlesByTag()
    }
  },

  async loadArticlesByTag(isLoadMore = false) {
    if (this.data.loading) return

    this.setData({ loading: true })
    try {
      const res = await articleAPI.getList({
        page: isLoadMore ? this.data.page : 1,
        size: this.data.size,
        tagId: this.data.selectedTag
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
    if (this.data.hasMore && !this.data.loading && this.data.selectedTag) {
      this.loadArticlesByTag(true)
    }
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/article?id=${id}`
    })
  }
})
