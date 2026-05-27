import request from "../request";
import type { ConfigItem } from "@/types";

export interface ConfigFormData {
  configKey: string;
  configValue: string;
}

export type ConfigListResponse = Promise<{
  data: { code: number; message: string; data: ConfigItem[] };
}>;
export type ConfigDetailResponse = Promise<{
  data: { code: number; message: string; data: ConfigItem };
}>;

export function getConfigList(): ConfigListResponse {
  return request.get("/v1/admin/configs");
}

export function getConfigDetail(key: string): ConfigDetailResponse {
  return request.get(`/v1/admin/configs/${key}`);
}

export function updateConfig(configs: ConfigFormData[]): Promise<{
  data: { code: number; message: string; data: ConfigFormData[] };
}> {
  return request.put("/v1/admin/configs", { configs });
}
