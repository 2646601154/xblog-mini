import request from '../request'
import type { ApiResponse } from '@/types'
import type { CategoryVO } from './article'

export type { CategoryVO }

// 分类列表
export function getCategoryList(): Promise<ApiResponse<CategoryVO[]>> {
  return request.get('/api/v1/categories')
}
