import request from '../request'
import type { LoginDTO, UserInfo } from '@/types'

export interface LoginVO {
  token: string
  user: UserInfo
}

export type LoginResponse = Promise<{ data: { code: number; message: string; data: LoginVO } }>

export function login(data: LoginDTO): LoginResponse {
  return request.post('/api/v1/auth/login', data)
}

export function getCurrentUser(): Promise<{ data: { code: number; message: string; data: UserInfo } }> {
  return request.get('/api/v1/auth/me')
}

export type { LoginDTO, UserInfo }