import service from '../request'
import type { Dashboard } from '@/types'

export type DashboardResponse = Promise<{
  data: { code: number; message: string; data: Dashboard }
}>

export function getDashboard(): DashboardResponse {
  return service.get('/v1/admin/dashboard')
}
