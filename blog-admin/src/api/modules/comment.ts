import service from '../request'
import type { Comment, PageResult, NullResponse } from '@/types'

export interface CommentQueryDTO {
  page?: number
  size?: number
  status?: string
  articleId?: number
  userId?: number
}

export type CommentListResponse = Promise<{
  data: { code: number; message: string; data: PageResult<Comment> }
}>
export type CommentStatusResponse = Promise<{
  data: { code: number; message: string; data: { id: number; status: string } }
}>

export function getCommentList(params: CommentQueryDTO): CommentListResponse {
  return service.get('/v1/admin/comments', { params })
}

export function approveComment(id: number): CommentStatusResponse {
  return service.put(`/v1/admin/comments/${id}/approve`)
}

export function rejectComment(id: number): CommentStatusResponse {
  return service.put(`/v1/admin/comments/${id}/reject`)
}

export function deleteComment(id: number): NullResponse {
  return service.delete(`/v1/admin/comments/${id}`)
}
