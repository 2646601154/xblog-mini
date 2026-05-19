import request from '../request'
import type { AxiosResponse } from 'axios'
import type { TagVO } from './article'

export type { TagVO }

export type TagListResponse = Promise<AxiosResponse<{ code: number; message: string; data: TagVO[] }>>

export function getTagList(): TagListResponse {
  return request.get('/v1/tags')
}
