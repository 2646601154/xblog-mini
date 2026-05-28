import service from '../request'
import type { MediaImage, PageResult, NullResponse } from '@/types'

export type MediaListResponse = Promise<{
  data: { code: number; message: string; data: PageResult<MediaImage> }
}>

export function getMediaList(params: { page: number; size: number }): MediaListResponse {
  return service.get('/v1/admin/media', { params })
}

export function deleteMediaImage(url: string): NullResponse {
  return service.delete('/v1/admin/media', { params: { url } })
}
