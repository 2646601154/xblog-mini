import service from '../request'
import type { ConfigItem } from '@/types'

export interface ConfigFormData {
  configKey: string
  configValue: string
}

export type ConfigListResponse = Promise<{
  data: { code: number; message: string; data: ConfigItem[] }
}>
export type ConfigUpdateResponse = Promise<{
  data: { code: number; message: string; data: ConfigFormData[] }
}>

export function getConfigList(): ConfigListResponse {
  return service.get('/v1/admin/configs')
}

export function updateConfig(configs: ConfigFormData[]): ConfigUpdateResponse {
  return service.put('/v1/admin/configs', { configs })
}
