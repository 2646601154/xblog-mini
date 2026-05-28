const ACCESS_TOKEN_KEY = 'admin_access_token'
const REFRESH_TOKEN_KEY = 'admin_refresh_token'
const USER_INFO_KEY = 'admin_user_info'

export const storage = {
  // Access Token (内存 + localStorage 双保险)
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
  },

  setAccessToken(token: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
  },

  removeAccessToken(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
  },

  // Refresh Token (长期存储)
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },

  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
  },

  removeRefreshToken(): void {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },

  // 兼容旧 API，将 getToken 指向 AccessToken
  getToken(): string | null {
    return this.getAccessToken()
  },

  setToken(token: string): void {
    this.setAccessToken(token)
  },

  getUserInfo<T>(): T | null {
    const data = localStorage.getItem(USER_INFO_KEY)
    if (!data) return null
    try {
      return JSON.parse(data) as T
    } catch {
      return null
    }
  },

  setUserInfo(userInfo: any): void {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  },

  clearAuth(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  },
}
