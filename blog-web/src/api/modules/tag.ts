import request from '../request'
import type { ApiResponse } from '@/types'
import type { TagVO } from './article'

export type { TagVO }

// 标签列表
export function getTagList(): Promise<ApiResponse<TagVO[]>> {
  return request.get('/api/v1/tags')
}
