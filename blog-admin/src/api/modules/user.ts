import service from '../request'
import type {
  User,
  UserFormData,
  CreateUserFormData,
  ResetPasswordFormData,
  PageResult,
  NullResponse,
} from '@/types'

export interface UserQuery {
  page?: number
  size?: number
  role?: string
  status?: string
}

export type UserListResponse = Promise<{
  data: { code: number; message: string; data: PageResult<User> }
}>
export type UserUpdateResponse = Promise<{
  data: { code: number; message: string; data: User }
}>
export type UserCreateResponse = Promise<{
  data: { code: number; message: string; data: User }
}>
export type UserStatusResponse = Promise<{
  data: { code: number; message: string; data: { id: number; status: string } }
}>

export function getUserList(params: UserQuery): UserListResponse {
  return service.get('/v1/admin/users', { params })
}

export function createUser(data: CreateUserFormData): UserCreateResponse {
  return service.post('/v1/admin/users', data)
}

export function updateUser(id: number, data: UserFormData): UserUpdateResponse {
  return service.put(`/v1/admin/users/${id}`, data)
}

export function disableUser(id: number): UserStatusResponse {
  return service.put(`/v1/admin/users/${id}/disable`)
}

export function enableUser(id: number): UserStatusResponse {
  return service.put(`/v1/admin/users/${id}/enable`)
}

export function deleteUser(id: number): NullResponse {
  return service.delete(`/v1/admin/users/${id}`)
}

export function resetPassword(id: number, data: ResetPasswordFormData): NullResponse {
  return service.put(`/v1/admin/users/${id}/reset-password`, data)
}
