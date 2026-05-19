export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  category: CategorySimple
  author: AuthorSimple
  tags: TagSimple[]
  status: 'draft' | 'published' | 'recycled'
  viewCount: number
  publishedAt: string
  createdAt: string
  updatedAt: string
}

export interface ArticleListItem {
  id: number
  title: string
  summary: string
  coverImage: string
  category: CategorySimple
  author: AuthorSimple
  tags: TagSimple[]
  status: 'draft' | 'published' | 'recycled'
  viewCount: number
  publishedAt: string
  createdAt: string
}

export interface ArticleFormData {
  title: string
  summary?: string
  content: string
  coverImage?: string
  categoryId: number
  tagIds?: number[]
  status?: 'draft' | 'published'
}

export interface CategorySimple {
  id: number
  name: string
  slug: string
}

export interface CategoryFormData {
  name: string
  slug: string
  description?: string
  sortOrder?: number
}

export interface AuthorSimple {
  id: number
  username: string
  nickname: string
  avatar: string
}

export interface TagSimple {
  id: number
  name: string
  slug: string
}

export interface TagFormData {
  name: string
  slug: string
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
  articleCount: number
  createdAt: string
}

export interface Comment {
  id: number
  article: { id: number; title: string }
  user: { id: number; username: string; nickname: string }
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
  updatedAt: string
}

export interface UserFormData {
  nickname: string
  email?: string
  avatar?: string
  role?: 'admin' | 'user'
  status?: 'normal' | 'disabled'
}

export interface CreateUserFormData {
  username: string
  password: string
  nickname?: string
  email?: string
  role?: 'admin' | 'user'
}

export interface ConfigItem {
  id: number
  configKey: string
  configValue: string
  description: string
  createdAt: string
  updatedAt: string
}

export interface ConfigFormData {
  configKey: string
  configValue: string
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

export interface LoginDTO {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: 'admin' | 'user'
  status: 'normal' | 'disabled'
  email?: string
}