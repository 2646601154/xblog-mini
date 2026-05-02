const TOKEN_KEY = 'xblog_token'
const USER_INFO_KEY = 'xblog_user_info'
const REMEMBER_KEY = 'xblog_remember'

interface RememberData {
  username: string
  password: string
}

export const storage = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
  },

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
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
    this.removeToken()
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
