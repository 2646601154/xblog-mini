import request from "../request";
import type { DashboardVo } from "@/types";

export type DashboardResponse = Promise<{
  data: { code: number; message: string; data: DashboardVo };
}>;

export function getDashboard(): DashboardResponse {
  return request.get("/v1/admin/dashboard");
}
