const ACCESS_TOKEN_KEY = 'xblog_access_token'
const REFRESH_TOKEN_KEY = 'xblog_refresh_token'
const USER_INFO_KEY = 'xblog_user_info'
const REMEMBER_KEY = 'xblog_remember'

interface RememberData {
  username: string
  password: string
}

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

  removeToken(): void {
    this.removeAccessToken()
  },

  getUserInfo<T = any>(): T | null {
    const info = localStorage.getItem(USER_INFO_KEY)
    return info ? JSON.parse(info) : null
  },

  setUserInfo<T = any>(userInfo: T): void {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  },

  removeUserInfo(): void {
    localStorage.removeItem(USER_INFO_KEY)
  },

  clearAuth(): void {
    this.removeAccessToken()
    this.removeRefreshToken()
    this.removeUserInfo()
  },

  getRemember(): RememberData | null {
    const data = localStorage.getItem(REMEMBER_KEY)
    return data ? JSON.parse(data) : null
  },

  setRemember(username: string, password: string): void {
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({ username, password }))
  },

  clearRemember(): void {
    localStorage.removeItem(REMEMBER_KEY)
  },
}
