import request from '../request'
import type { Comment, PageResult } from '@/types'

export interface CommentQueryDTO {
  page?: number
  size?: number
  status?: string
  articleId?: number
  userId?: number
}

export type CommentListResponse = Promise<{ data: { code: number; message: string; data: PageResult<Comment> } }>

export function getCommentList(params: CommentQueryDTO): CommentListResponse {
  return request.get('/api/v1/admin/comments', { params })
}

export function approveComment(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/api/v1/admin/comments/${id}/approve`)
}

export function rejectComment(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/api/v1/admin/comments/${id}/reject`)
}

export function deleteComment(id: number): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/api/v1/admin/comments/${id}`)
}