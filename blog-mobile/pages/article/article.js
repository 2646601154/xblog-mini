/**
 * 文章详情页
 */
const articleAPI = require('../../services/article')
const commentAPI = require('../../services/comment')
const app = getApp()

Page({
  data: {
    article: null,
    comments: [],
    prevNext: {},
    commentPage: 1,
    commentSize: 20,
    hasMoreComment: true,
    loading: true,
    commentText: '',
    submitting: false,
    isLoggedIn: false
  },

  onLoad(options) {
    const { id } = options
    if (!id) {
      wx.showToast({ title: '文章不存在', icon: 'none' })
      wx.navigateBack()
      return
    }
    this.articleId = id
    this.checkLogin()
    this.loadArticle()
    this.loadComments()
  },

  checkLogin() {
    const token = app.globalData.accessToken
    this.setData({ isLoggedIn: !!token })
  },

  async loadArticle() {
    try {
      const [article, prevNext] = await Promise.all([
        articleAPI.getDetail(this.articleId),
        articleAPI.getPrevNext(this.articleId)
      ])
      this.setData({
        article,
        prevNext,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async loadComments(isLoadMore = false) {
    if (this.data.loading) return

    const page = isLoadMore ? this.data.commentPage : 1
    if (!isLoadMore) {
      this.setData({ comments: [] })
    }

    try {
      const res = await articleAPI.getComments(this.articleId, {
        page,
        size: this.data.commentSize
      })
      const comments = isLoadMore
        ? [...this.data.comments, ...res.records]
        : res.records

      this.setData({
        comments,
        commentPage: page + 1,
        hasMoreComment: comments.length < res.total
      })
    } catch (e) {
      // 忽略评论加载错误
    }
  },

  onReachBottom() {
    if (this.data.hasMoreComment) {
      this.loadComments(true)
    }
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  async submitComment() {
    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const { commentText } = this.data
    if (!commentText.trim()) {
      wx.showToast({ title: '请输入评论', icon: 'none' })
      return
    }

    if (this.data.submitting) return
    this.setData({ submitting: true })

    try {
      await commentAPI.publish({
        articleId: this.articleId,
        content: commentText.trim()
      })
      wx.showToast({ title: '评论成功', icon: 'success' })
      this.setData({ commentText: '' })
      // 刷新评论列表
      this.setData({ commentPage: 1, comments: [] })
      this.loadComments()
    } catch (e) {
      // 错误已由 request 处理
    } finally {
      this.setData({ submitting: false })
    }
  },

  goToArticle(e) {
    const { id } = e.currentTarget.dataset
    if (id) {
      wx.navigateTo({ url: `/pages/article/article?id=${id}` })
    }
  },

  onShareAppMessage() {
    const { article } = this.data
    return {
      title: article?.title || '博客文章',
      path: `/pages/article/article?id=${this.articleId}`,
      imageUrl: article?.coverImage
    }
  },

  onShareTimeline() {
    const { article } = this.data
    return {
      title: article?.title || '博客文章',
      query: `id=${this.articleId}`,
      imageUrl: article?.coverImage
    }
  }
})
