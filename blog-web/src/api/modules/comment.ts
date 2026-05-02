import request from '../request'
import type { ApiResponse, PageResult } from '@/types'

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

export interface CommentListVO extends PageResult<CommentVO> {}

// 发表评论
export function postComment(data: CommentDTO): Promise<ApiResponse<CommentVO>> {
  return request.post('/api/v1/comments', data)
}

// 文章评论列表
export function getArticleComments(
  articleId: number,
  params?: { page?: number; size?: number }
): Promise<ApiResponse<CommentListVO>> {
  return request.get(`/api/v1/articles/${articleId}/comments`, { params })
}
