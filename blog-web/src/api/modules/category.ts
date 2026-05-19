import request from '../request'
import type { AxiosResponse } from 'axios'
import type { CategoryVO } from './article'

export type { CategoryVO }

export interface CategoryDetailVO extends CategoryVO {
  articleCount: number
  createdAt: string
}

export type CategoryListResponse = Promise<AxiosResponse<{ code: number; message: string; data: CategoryVO[] }>>
export type CategoryDetailResponse = Promise<AxiosResponse<{ code: number; message: string; data: CategoryDetailVO }>>

export function getCategoryList(): CategoryListResponse {
  return request.get('/v1/categories')
}

export function getCategoryBySlug(slug: string): CategoryDetailResponse {
  return request.get(`/v1/categories/${slug}`)
}
