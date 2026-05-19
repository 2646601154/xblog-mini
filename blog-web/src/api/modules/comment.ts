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
  return request.post('/v1/comments', data)
}

export function getArticleComments(
  articleId: number,
  params?: { page?: number; size?: number }
): CommentListResponse {
  return request.get(`/v1/articles/${articleId}/comments`, { params })
}

// 我的评论
export interface CommentMyVO {
  id: number
  article: { id: number; title: string }
  content: string
  status: string
  createdAt: string
  updatedAt: string
}

export type MyCommentListVO = PageResult<CommentMyVO>

export type MyCommentListResponse = Promise<AxiosResponse<{ code: number; message: string; data: MyCommentListVO }>>

export function getMyComments(params?: { page?: number; size?: number }): MyCommentListResponse {
  return request.get('/v1/comments/my', { params })
}
