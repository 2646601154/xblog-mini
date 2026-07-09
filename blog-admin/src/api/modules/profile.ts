import service from '../request'
import type { UpdateProfileParams, ChangePasswordParams, NullResponse } from '@/types'

// 修改个人资料
export function updateProfile(data: UpdateProfileParams): NullResponse {
  return service.put('/v1/users/profile', data)
}

// 修改密码
export function changePassword(data: ChangePasswordParams): NullResponse {
  return service.put('/v1/users/password', data)
}
