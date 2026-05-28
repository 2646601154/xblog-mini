import service from '../request'
import type { Tag, NullResponse } from '@/types'

export interface TagFormData {
  name: string
  slug: string
}

export type TagListResponse = Promise<{
  data: { code: number; message: string; data: Tag[] }
}>
export type TagCreateResponse = Promise<{
  data: { code: number; message: string; data: Tag }
}>

export function getTagList(): TagListResponse {
  return service.get('/v1/admin/tags')
}

export function createTag(data: TagFormData): TagCreateResponse {
  return service.post('/v1/admin/tags', data)
}

export function updateTag(id: number, data: TagFormData): TagCreateResponse {
  return service.put(`/v1/admin/tags/${id}`, data)
}

export function deleteTag(id: number): NullResponse {
  return service.delete(`/v1/admin/tags/${id}`)
}
