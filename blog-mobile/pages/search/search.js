/**
 * 搜索页
 */
const articleAPI = require('../../services/article')

Page({
  data: {
    searchKeyword: '',
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    searchHistory: [],
    noResult: false
  },

  onLoad() {
    // 加载搜索历史
    const history = wx.getStorageSync('search_history') || []
    this.setData({ searchHistory: history })
  },

  onKeywordInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  async onSearch() {
    const keyword = this.data.searchKeyword.trim()
    if (!keyword) {
      wx.showToast({ title: '请输入关键词', icon: 'none' })
      return
    }

    this.setData({ page: 1, articles: [], hasMore: true, noResult: false })
    this.saveSearchHistory(keyword)
    this.doSearch()
  },

  async doSearch(isLoadMore = false) {
    if (this.data.loading) return
    const keyword = this.data.searchKeyword.trim()

    this.setData({ loading: true })
    try {
      // TODO: 后端需支持标题搜索
      const res = await articleAPI.getList({
        page: isLoadMore ? this.data.page : 1,
        size: this.data.size
      })

      // 前端过滤（临时方案）
      const filtered = res.records.filter(item =>
        item.title.includes(keyword) || item.summary.includes(keyword)
      )

      const articles = isLoadMore
        ? [...this.data.articles, ...filtered]
        : filtered

      this.setData({
        articles,
        page: this.data.page + 1,
        hasMore: articles.length < res.total && filtered.length === this.data.size,
        loading: false,
        noResult: articles.length === 0
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.doSearch(true)
    }
  },

  saveSearchHistory(keyword) {
    let history = this.data.searchHistory
    history = history.filter(item => item !== keyword)
    history.unshift(keyword)
    history = history.slice(0, 10) // 最多保留10条
    this.setData({ searchHistory: history })
    wx.setStorageSync('search_history', history)
  },

  clearHistory() {
    this.setData({ searchHistory: [] })
    wx.removeStorageSync('search_history')
  },

  useHistory(e) {
    const { keyword } = e.currentTarget.dataset
    this.setData({ searchKeyword: keyword })
    this.onSearch()
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/article?id=${id}`
    })
  }
})
