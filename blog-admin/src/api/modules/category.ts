import service from '../request'
import type { Category, NullResponse } from '@/types'

export interface CategoryFormData {
  name: string
  slug: string
  description?: string
  sortOrder?: number
}

export type CategoryListResponse = Promise<{
  data: { code: number; message: string; data: Category[] }
}>
export type CategoryCreateResponse = Promise<{
  data: { code: number; message: string; data: Category }
}>

export function getCategoryList(): CategoryListResponse {
  return service.get('/v1/admin/categories')
}

export function createCategory(data: CategoryFormData): CategoryCreateResponse {
  return service.post('/v1/admin/categories', data)
}

export function updateCategory(id: number, data: CategoryFormData): CategoryCreateResponse {
  return service.put(`/v1/admin/categories/${id}`, data)
}

export function deleteCategory(id: number): NullResponse {
  return service.delete(`/v1/admin/categories/${id}`)
}
