import service from '../request'
import type { ArticleListItem, ArticleFormData, PageResult, NullResponse } from '@/types'

export interface ArticleQuery {
  page?: number
  size?: number
  status?: string
  categoryId?: number
  title?: string
}

export type ArticleListResponse = Promise<{
  data: { code: number; message: string; data: PageResult<ArticleListItem> }
}>
export type ArticleDetailResponse = Promise<{
  data: { code: number; message: string; data: any }
}>
export type ArticleIdResponse = Promise<{
  data: { code: number; message: string; data: { id: number } }
}>
export type ArticleStatusResponse = Promise<{
  data: { code: number; message: string; data: { id: number; status: string } }
}>
export type ArticleTagsResponse = Promise<{
  data: { code: number; message: string; data: any[] }
}>

export function getArticleList(params: ArticleQuery): ArticleListResponse {
  return service.get('/v1/admin/articles', { params })
}

export function getArticleDetail(id: number): ArticleDetailResponse {
  // 管理端专用接口，支持 draft/recycled；勿改用公开 GET /v1/articles/{id}（BUG-4：公开接口每次请求会增加浏览量）
  return service.get(`/v1/admin/articles/${id}`)
}

export function createArticle(data: ArticleFormData): ArticleIdResponse {
  return service.post('/v1/admin/articles', data)
}

export function updateArticle(id: number, data: ArticleFormData): ArticleIdResponse {
  return service.put(`/v1/admin/articles/${id}`, data)
}

export function publishArticle(id: number): ArticleStatusResponse {
  return service.put(`/v1/admin/articles/${id}/publish`)
}

export function recycleArticle(id: number): ArticleStatusResponse {
  return service.put(`/v1/admin/articles/${id}/recycle`)
}

export function restoreArticle(id: number): ArticleStatusResponse {
  return service.put(`/v1/admin/articles/${id}/restore`)
}

export function deleteArticle(id: number): NullResponse {
  return service.delete(`/v1/admin/articles/${id}`)
}

export function bindArticleTags(id: number, tagIds: number[]): ArticleTagsResponse {
  return service.post(`/v1/admin/articles/${id}/tags`, tagIds)
}
