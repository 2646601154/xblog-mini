import request from '../request'
import type { Category } from '@/types'

export interface CategoryFormData {
  name: string
  slug: string
  description?: string
  sortOrder?: number
}

export type CategoryListResponse = Promise<{ data: { code: number; message: string; data: Category[] } }>
export type CategoryCreateResponse = Promise<{ data: { code: number; message: string; data: Category } }>

export function getCategoryList(): CategoryListResponse {
  return request.get('/api/v1/admin/categories')
}

export function createCategory(data: CategoryFormData): CategoryCreateResponse {
  return request.post('/api/v1/admin/categories', data)
}

export function updateCategory(id: number, data: CategoryFormData): CategoryCreateResponse {
  return request.put(`/api/v1/admin/categories/${id}`, data)
}

export function deleteCategory(id: number): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/api/v1/admin/categories/${id}`)
}