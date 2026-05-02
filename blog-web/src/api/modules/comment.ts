import request from '../request'
import type { PageResult } from '@/types'
import type { AxiosResponse } from 'axios'

export interface CommentDTO {
  articleId: number
  content: string
}

export interface CommentUserVO {
  id: number
  username: string
  nickname: string
  avatar: string
}

export interface CommentVO {
  id: number
  content: string
  user: CommentUserVO
  createdAt: string
}

export type CommentListVO = PageResult<CommentVO>

export type CommentListResponse = Promise<AxiosResponse<{ code: number; message: string; data: CommentListVO }>>
export type CommentResponse = Promise<AxiosResponse<{ code: number; message: string; data: CommentVO }>>

export function postComment(data: CommentDTO): CommentResponse {
  return request.post('/api/v1/comments', data)
}

export function getArticleComments(
  articleId: number,
  params?: { page?: number; size?: number }
): CommentListResponse {
  return request.get(`/api/v1/articles/${articleId}/comments`, { params })
}
