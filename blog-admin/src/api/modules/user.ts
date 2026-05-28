import request from '../request'
import type {
  User,
  UserFormData,
  CreateUserFormData,
  ResetPasswordFormData,
  PageResult,
} from '@/types'

export interface UserQueryDTO {
  page?: number
  size?: number
  role?: string
  status?: string
}

export type UserListResponse = Promise<{
  data: { code: number; message: string; data: PageResult<User> }
}>
export type UserDetailResponse = Promise<{
  data: { code: number; message: string; data: User }
}>
export type UserUpdateResponse = Promise<{
  data: { code: number; message: string; data: User }
}>
export type UserCreateResponse = Promise<{
  data: { code: number; message: string; data: User }
}>

export function getUserList(params: UserQueryDTO): UserListResponse {
  return request.get('/v1/admin/users', { params })
}

export function getUserDetail(id: number): UserDetailResponse {
  return request.get(`/v1/admin/users/${id}`)
}

export function createUser(data: CreateUserFormData): UserCreateResponse {
  return request.post('/v1/admin/users', data)
}

export function updateUser(id: number, data: UserFormData): UserUpdateResponse {
  return request.put(`/v1/admin/users/${id}`, data)
}

export function disableUser(id: number): Promise<{
  data: { code: number; message: string; data: { id: number; status: string } }
}> {
  return request.put(`/v1/admin/users/${id}/disable`)
}

export function enableUser(id: number): Promise<{
  data: { code: number; message: string; data: { id: number; status: string } }
}> {
  return request.put(`/v1/admin/users/${id}/enable`)
}

export function deleteUser(
  id: number,
): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.delete(`/v1/admin/users/${id}`)
}

export function resetPassword(
  id: number,
  data: ResetPasswordFormData,
): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.put(`/v1/admin/users/${id}/reset-password`, data)
}
