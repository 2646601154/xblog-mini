import request from '../request'
import type { AxiosResponse } from 'axios'
import type { SiteConfig } from '@/types'

export type ConfigResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: SiteConfig }>
>

export function getPublicConfig(): ConfigResponse {
  return request.get('/v1/configs')
}
