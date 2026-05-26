import request from '../request'
import type { ApiResponse } from '@/types'

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  nickname: string
  email?: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: 'admin' | 'user'
  status: 'normal' | 'disabled'
  email?: string
}

/**
 * 双 Token 响应
 */
export interface TokenVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export type LoginResponse = Promise<{ data: { code: number; message: string; data: TokenVO } }>
export type RegisterResponse = Promise<{ data: { code: number; message: string; data: UserInfo } }>
export type UserInfoResponse = Promise<{ data: { code: number; message: string; data: UserInfo } }>

// 登录
export function login(data: LoginDTO): LoginResponse {
  return request.post('/v1/auth/login', data)
}

// 注册
export function register(data: RegisterDTO): RegisterResponse {
  return request.post('/v1/auth/register', data)
}

// 刷新 Access Token
export interface RefreshTokenDTO {
  refreshToken: string
}

export function refreshToken(data: RefreshTokenDTO): LoginResponse {
  return request.post('/v1/auth/refresh', data)
}

// 登出
export function logout(): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.post('/v1/auth/logout')
}

// 获取当前用户
export function getCurrentUser(): UserInfoResponse {
  return request.get('/v1/auth/me')
}

// 修改个人资料
export interface UpdateProfileDTO {
  nickname?: string
  avatar?: string
  email?: string
}

export function updateProfile(data: UpdateProfileDTO): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.put('/v1/users/profile', data)
}

// 修改密码
export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
}

export function changePassword(data: ChangePasswordDTO): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.put('/v1/users/password', data)
}
