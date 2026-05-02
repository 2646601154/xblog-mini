import request from '../request'
import type { ApiResponse, PageResult } from '@/types'

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

export interface LoginVO {
  token: string
  user: UserInfo
}

// 登录
export function login(data: LoginDTO): Promise<ApiResponse<LoginVO>> {
  return request.post('/api/v1/auth/login', data)
}

// 注册
export function register(data: RegisterDTO): Promise<ApiResponse<UserInfo>> {
  return request.post('/api/v1/auth/register', data)
}

// 获取当前用户
export function getCurrentUser(): Promise<ApiResponse<UserInfo>> {
  return request.get('/api/v1/auth/me')
}
