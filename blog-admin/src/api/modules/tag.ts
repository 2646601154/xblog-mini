import request from '../request'
import type { Tag } from '@/types'

export interface TagFormData {
  name: string
  slug: string
}

export type TagListResponse = Promise<{ data: { code: number; message: string; data: Tag[] } }>
export type TagCreateResponse = Promise<{ data: { code: number; message: string; data: Tag } }>

export function getTagList(): TagListResponse {
  return request.get('/api/v1/admin/tags')
}

export function createTag(data: TagFormData): TagCreateResponse {
  return request.post('/api/v1/admin/tags', data)
}

export function updateTag(id: number, data: TagFormData): TagCreateResponse {
  return request.put(`/api/v1/admin/tags/${id}`, data)
}

export function deleteTag(id: number): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/api/v1/admin/tags/${id}`)
}