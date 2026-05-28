import request from '../request'
import type { MediaImage, PageResult } from '@/types'

export function getMediaList(params: { page: number; size: number }) {
  return request.get<{
    code: number
    message: string
    data: PageResult<MediaImage>
  }>('/v1/admin/media', { params })
}

export function deleteMediaImage(url: string) {
  return request.delete('/v1/admin/media', { params: { url } })
}
