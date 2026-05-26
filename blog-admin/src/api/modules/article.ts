import request from '../request'
import type { ArticleListItem, ArticleFormData, PageResult } from '@/types'

export interface ArticleQueryDTO {
  page?: number
  size?: number
  status?: string
  categoryId?: number
  title?: string
}

export type ArticleListResponse = Promise<{ data: { code: number; message: string; data: PageResult<ArticleListItem> } }>
export type ArticleDetailResponse = Promise<{ data: { code: number; message: string; data: any } }>

export function getArticleList(params: ArticleQueryDTO): ArticleListResponse {
  return request.get('/v1/admin/articles', { params })
}

export function getArticleDetail(id: number): ArticleDetailResponse {
  // 管理端专用接口，支持 draft/recycled；勿改用公开 GET /v1/articles/{id}（BUG-4：公开接口每次请求会增加浏览量）
  return request.get(`/v1/admin/articles/${id}`)
}

export function createArticle(data: ArticleFormData): Promise<{ data: { code: number; message: string; data: { id: number } } }> {
  return request.post('/v1/admin/articles', data)
}

export function updateArticle(id: number, data: ArticleFormData): Promise<{ data: { code: number; message: string; data: { id: number } } }> {
  return request.put(`/v1/admin/articles/${id}`, data)
}

export function publishArticle(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/v1/admin/articles/${id}/publish`)
}

export function recycleArticle(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/v1/admin/articles/${id}/recycle`)
}

export function restoreArticle(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/v1/admin/articles/${id}/restore`)
}

export function deleteArticle(id: number): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/v1/admin/articles/${id}`)
}

export function bindArticleTags(id: number, tagIds: number[]): Promise<{ data: { code: number; message: string; data: any[] } }> {
  return request.post(`/v1/admin/articles/${id}/tags`, tagIds)
}