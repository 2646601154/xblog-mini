import request from '../request'
import type { User, UserFormData, PageResult } from '@/types'

export interface UserQueryDTO {
  page?: number
  size?: number
  role?: string
  status?: string
}

export type UserListResponse = Promise<{ data: { code: number; message: string; data: PageResult<User> } }>
export type UserDetailResponse = Promise<{ data: { code: number; message: string; data: User } }>
export type UserUpdateResponse = Promise<{ data: { code: number; message: string; data: User } }>

export function getUserList(params: UserQueryDTO): UserListResponse {
  return request.get('/api/v1/admin/users', { params })
}

export function getUserDetail(id: number): UserDetailResponse {
  return request.get(`/api/v1/admin/users/${id}`)
}

export function updateUser(id: number, data: UserFormData): UserUpdateResponse {
  return request.put(`/api/v1/admin/users/${id}`, data)
}

export function disableUser(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/api/v1/admin/users/${id}/disable`)
}

export function enableUser(id: number): Promise<{ data: { code: number; message: string; data: { id: number; status: string } } }> {
  return request.put(`/api/v1/admin/users/${id}/enable`)
}

export function deleteUser(id: number): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/api/v1/admin/users/${id}`)
}