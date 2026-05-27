import request from "../request";
import type { Login, UserInfo } from "@/types";

/**
 * 双 Token 响应
 */
export interface Token {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export type LoginResponse = Promise<{
  data: { code: number; message: string; data: Token };
}>;

export function login(data: Login): LoginResponse {
  return request.post("/v1/auth/login", data);
}

export function getCurrentUser(): Promise<{
  data: { code: number; message: string; data: UserInfo };
}> {
  return request.get("/v1/auth/me");
}

// 刷新 Access Token
export interface RefreshToken {
  refreshToken: string;
}

export function refreshToken(data: RefreshToken): LoginResponse {
  return request.post("/v1/auth/refresh", data);
}

// 登出
export function logout(): Promise<{
  data: { code: number; message: string; data: null };
}> {
  return request.post("/v1/auth/logout");
}

export type { Login, UserInfo };
