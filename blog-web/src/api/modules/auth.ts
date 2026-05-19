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