/**
 * 首页 - 文章列表
 */
const articleAPI = require('../../services/article')

Page({
  data: {
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    categories: [],
    tags: [],
    selectedCategory: null,
    selectedTag: null,
    showFilter: false
  },

  onLoad() {
    this.loadArticles()
    this.loadCategories()
    this.loadTags()
  },

  onPullDownRefresh() {
    this.setData({ page: 1, articles: [], hasMore: true })
    this.loadArticles().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadArticles(true)
    }
  },

  async loadArticles(isLoadMore = false) {
    if (this.data.loading) return
    if (!isLoadMore) {
      this.setData({ page: 1, articles: [] })
    }

    this.setData({ loading: true })

    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      }
      if (this.data.selectedCategory) {
        params.categoryId = this.data.selectedCategory
      }
      if (this.data.selectedTag) {
        params.tagId = this.data.selectedTag
      }

      const res = await articleAPI.getList(params)
      const list = isLoadMore
        ? [...this.data.articles, ...res.records]
        : res.records

      this.setData({
        articles: list,
        page: this.data.page + 1,
        hasMore: list.length < res.total,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  async loadCategories() {
    // TODO: 调用分类接口
    // const res = await categoryAPI.getCategories()
    // this.setData({ categories: res })
  },

  async loadTags() {
    // TODO: 调用标签接口
    // const res = await tagAPI.getTags()
    // this.setData({ tags: res })
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/article?id=${id}`
    })
  },

  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter })
  },

  selectCategory(e) {
    const { id } = e.currentTarget.dataset
    this.setData({
      selectedCategory: id,
      showFilter: false,
      page: 1
    })
    this.loadArticles()
  },

  selectTag(e) {
    const { id } = e.currentTarget.dataset
    this.setData({
      selectedTag: id,
      showFilter: false,
      page: 1
    })
    this.loadArticles()
  },

  clearFilter() {
    this.setData({
      selectedCategory: null,
      selectedTag: null,
      page: 1
    })
    this.loadArticles()
  },

  onShareAppMessage() {
    return {
      title: '博客文章',
      path: '/pages/index/index'
    }
  }
})
