import request from '../request'
import type { LoginDTO, UserInfo } from '@/types'

/**
 * 双 Token 响应
 */
export interface TokenVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export type LoginResponse = Promise<{ data: { code: number; message: string; data: TokenVO } }>

export function login(data: LoginDTO): LoginResponse {
  return request.post('/api/v1/auth/login', data)
}

export function getCurrentUser(): Promise<{ data: { code: number; message: string; data: UserInfo } }> {
  return request.get('/api/v1/auth/me')
}

// 刷新 Access Token
export interface RefreshTokenDTO {
  refreshToken: string
}

export function refreshToken(data: RefreshTokenDTO): LoginResponse {
  return request.post('/api/v1/auth/refresh', data)
}

// 登出
export function logout(): Promise<{ data: { code: number; message: string; data: null } }> {
  return request.post('/api/v1/auth/logout')
}

export type { LoginDTO, UserInfo }