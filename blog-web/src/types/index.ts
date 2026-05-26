export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  categoryId: number
  categoryName: string
  categorySlug: string
  authorId: number
  authorName: string
  authorAvatar: string
  status: 'draft' | 'published' | 'recycled'
  viewCount: number
  commentCount: number
  tags: Tag[]
  createdAt: string
  publishedAt: string
}

export interface Category {
  id: number
  name: string
  slug: string
  description: string
  sortOrder: number
  articleCount: number
  createdAt: string
}

export interface Tag {
  id: number
  name: string
  slug: string
  createdAt: string
}

export interface Comment {
  id: number
  articleId: number
  userId: number
  userName: string
  userAvatar: string
  content: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
}

export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  role: 'admin' | 'user'
  status: 'normal' | 'disabled'
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface SiteConfig {
  icpNumber: string
  copyright: string
}
