import service from '../request'
import type { Login, UserInfo, NullResponse } from '@/types'

/**
 * 双 Token 响应
 */
interface Token {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export type LoginResponse = Promise<{
  data: { code: number; message: string; data: Token }
}>
export type UserinfoResponse = Promise<{
  data: { code: number; message: string; data: UserInfo }
}>

export function login(data: Login): LoginResponse {
  return service.post('/v1/auth/login', data)
}

export function getCurrentUser(): UserinfoResponse {
  return service.get('/v1/auth/me')
}

// 刷新 Access Token
export interface RefreshToken {
  refreshToken: string
}

export function refreshToken(data: RefreshToken): LoginResponse {
  return service.post('/v1/auth/refresh', data)
}

// 登出
export function logout(): NullResponse {
  return service.post('/v1/auth/logout')
}

export type { Login, UserInfo }
