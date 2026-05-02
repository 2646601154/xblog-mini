import request from '../request'
import type { ApiResponse, PageResult } from '@/types'

export interface CategoryVO {
  id: number
  name: string
  slug: string
  description?: string
}

export interface AuthorVO {
  id: number
  username: string
  nickname: string
  avatar: string
}

export interface TagVO {
  id: number
  name: string
  slug: string
}

export interface ArticleListDTO {
  page?: number
  size?: number
  categoryId?: number
  tagId?: number
}

export interface ArticleListItemVO {
  id: number
  title: string
  summary: string
  coverImage: string
  category: CategoryVO
  author: AuthorVO
  tags: TagVO[]
  viewCount: number
  publishedAt: string
  createdAt: string
}

export interface ArticleListVO extends PageResult<ArticleListItemVO> {}

export interface ArticleDetailVO {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  category: CategoryVO
  author: AuthorVO
  tags: TagVO[]
  status: string
  viewCount: number
  publishedAt: string
  createdAt: string
  updatedAt: string
}

// 文章列表
export function getArticleList(params: ArticleListDTO): Promise<ApiResponse<ArticleListVO>> {
  return request.get('/api/v1/articles', { params })
}

// 文章详情
export function getArticleDetail(id: number): Promise<ApiResponse<ArticleDetailVO>> {
  return request.get(`/api/v1/articles/${id}`)
}
