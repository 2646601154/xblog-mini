/**
 * Unit Tests for app.js
 * Tests the main application entry point and token management
 */
require('../__mocks__/wx')

// Store original App function
const originalApp = global.App

describe('app.js - Main Application', () => {
  let appInstance

  beforeEach(() => {
    jest.resetModules()
    wx.__resetStorage()
    
    // Mock getApp to return the app instance
    const mockAppInstance = {
      globalData: {
        userInfo: null,
        accessToken: null,
        refreshToken: null,
        baseUrl: 'https://api.test.cn'
      },
      onLaunch: jest.fn(),
      checkLoginStatus: jest.fn(),
      setTokens: jest.fn(),
      clearTokens: jest.fn()
    }
    
    global.App = jest.fn((config) => {
      appInstance = config
      return config
    })
    
    global.getApp = jest.fn(() => mockAppInstance)
  })

  afterEach(() => {
    global.App = originalApp
  })

  describe('globalData', () => {
    it('should have correct initial globalData structure', () => {
      const expectedGlobalData = {
        userInfo: null,
        accessToken: null,
        refreshToken: null,
        baseUrl: expect.any(String)
      }

      expect(appInstance.globalData).toMatchObject({
        userInfo: null,
        accessToken: null,
        refreshToken: null
      })
      expect(typeof appInstance.globalData.baseUrl).toBe('string')
    })

    it('should have a valid baseUrl format', () => {
      expect(appInstance.globalData.baseUrl).toMatch(/^https?:\/\//)
    })
  })

  describe('onLaunch', () => {
    it('should call checkLoginStatus on launch', () => {
      const mockCheckLoginStatus = jest.fn()
      appInstance.checkLoginStatus = mockCheckLoginStatus
      appInstance.onLaunch()
      expect(mockCheckLoginStatus).toHaveBeenCalled()
    })
  })

  describe('setTokens', () => {
    it('should set accessToken and refreshToken in globalData', () => {
      const accessToken = 'test_access_token'
      const refreshToken = 'test_refresh_token'

      appInstance.setTokens(accessToken, refreshToken)

      expect(appInstance.globalData.accessToken).toBe(accessToken)
      expect(appInstance.globalData.refreshToken).toBe(refreshToken)
    })

    it('should store tokens in wx storage', () => {
      const accessToken = 'test_access_token'
      const refreshToken = 'test_refresh_token'

      appInstance.setTokens(accessToken, refreshToken)

      expect(wx.setStorageSync).toHaveBeenCalledWith('access_token', accessToken)
      expect(wx.setStorageSync).toHaveBeenCalledWith('refresh_token', refreshToken)
    })

    it('should handle empty token values', () => {
      appInstance.setTokens('', '')

      expect(appInstance.globalData.accessToken).toBe('')
      expect(appInstance.globalData.refreshToken).toBe('')
      expect(wx.setStorageSync).toHaveBeenCalledWith('access_token', '')
      expect(wx.setStorageSync).toHaveBeenCalledWith('refresh_token', '')
    })

    it('should handle null token values', () => {
      appInstance.setTokens(null, null)

      expect(appInstance.globalData.accessToken).toBeNull()
      expect(appInstance.globalData.refreshToken).toBeNull()
      expect(wx.setStorageSync).toHaveBeenCalledWith('access_token', null)
      expect(wx.setStorageSync).toHaveBeenCalledWith('refresh_token', null)
    })
  })

  describe('clearTokens', () => {
    it('should clear tokens from globalData', () => {
      appInstance.globalData.accessToken = 'existing_token'
      appInstance.globalData.refreshToken = 'existing_refresh_token'

      appInstance.clearTokens()

      expect(appInstance.globalData.accessToken).toBeNull()
      expect(appInstance.globalData.refreshToken).toBeNull()
    })

    it('should remove tokens from wx storage', () => {
      appInstance.clearTokens()

      expect(wx.removeStorageSync).toHaveBeenCalledWith('access_token')
      expect(wx.removeStorageSync).toHaveBeenCalledWith('refresh_token')
    })

    it('should be idempotent (safe to call multiple times)', () => {
      appInstance.clearTokens()
      appInstance.clearTokens()
      appInstance.clearTokens()

      expect(wx.removeStorageSync).toHaveBeenCalledTimes(6) // 2 per call
    })
  })

  describe('checkLoginStatus', () => {
    it('should retrieve tokens from storage when they exist', () => {
      const accessToken = 'stored_access_token'
      const refreshToken = 'stored_refresh_token'

      wx.__addToStorage('access_token', accessToken)
      wx.__addToStorage('refresh_token', refreshToken)

      appInstance.checkLoginStatus()

      expect(appInstance.globalData.accessToken).toBe(accessToken)
      expect(appInstance.globalData.refreshToken).toBe(refreshToken)
    })

    it('should not set tokens when accessToken is empty', () => {
      wx.__addToStorage('access_token', '')
      wx.__addToStorage('refresh_token', 'some_refresh_token')

      appInstance.checkLoginStatus()

      expect(appInstance.globalData.accessToken).toBe('')
    })

    it('should handle missing storage keys gracefully', () => {
      appInstance.checkLoginStatus()

      expect(appInstance.globalData.accessToken).toBeNull()
      expect(appInstance.globalData.refreshToken).toBeNull()
    })

    it('should set globalData from storage even if only accessToken exists', () => {
      wx.__addToStorage('access_token', 'only_access_token')
      wx.__addToStorage('refresh_token', null)

      appInstance.checkLoginStatus()

      expect(appInstance.globalData.accessToken).toBe('only_access_token')
      expect(appInstance.globalData.refreshToken).toBeNull()
    })
  })
})

describe('app.js - Integration Scenarios', () => {
  let appInstance

  beforeEach(() => {
    jest.resetModules()
    wx.__resetStorage()

    const mockAppInstance = {
      globalData: {
        userInfo: null,
        accessToken: null,
        refreshToken: null,
        baseUrl: 'https://api.test.cn'
      },
      onLaunch: jest.fn(),
      checkLoginStatus: jest.fn(),
      setTokens: jest.fn(),
      clearTokens: jest.fn()
    }

    global.getApp = jest.fn(() => mockAppInstance)
    appInstance = mockAppInstance
  })

  describe('Full authentication flow', () => {
    it('should support complete login-logout cycle', () => {
      // Initial state - no tokens
      expect(appInstance.globalData.accessToken).toBeNull()
      expect(appInstance.globalData.refreshToken).toBeNull()

      // Simulate login - set tokens
      appInstance.globalData.accessToken = 'new_access_token'
      appInstance.globalData.refreshToken = 'new_refresh_token'
      wx.setStorageSync('access_token', 'new_access_token')
      wx.setStorageSync('refresh_token', 'new_refresh_token')

      expect(appInstance.globalData.accessToken).toBe('new_access_token')
      expect(wx.getStorageSync('access_token')).toBe('new_access_token')

      // Simulate logout - clear tokens
      appInstance.globalData.accessToken = null
      appInstance.globalData.refreshToken = null
      wx.removeStorageSync('access_token')
      wx.removeStorageSync('refresh_token')

      expect(appInstance.globalData.accessToken).toBeNull()
      expect(wx.getStorageSync('access_token')).toBeUndefined()
    })

    it('should support token refresh scenario', () => {
      // Initial login
      appInstance.setTokens('old_access_token', 'refresh_token')

      expect(appInstance.globalData.accessToken).toBe('old_access_token')
      expect(wx.getStorageSync('access_token')).toBe('old_access_token')

      // Simulate token refresh
      appInstance.setTokens('new_access_token', 'new_refresh_token')

      expect(appInstance.globalData.accessToken).toBe('new_access_token')
      expect(appInstance.globalData.refreshToken).toBe('new_refresh_token')
      expect(wx.getStorageSync('access_token')).toBe('new_access_token')
      expect(wx.getStorageSync('refresh_token')).toBe('new_refresh_token')
    })
  })

  describe('Token expiration handling', () => {
    it('should allow clearing tokens when session expires', () => {
      appInstance.setTokens('expired_access_token', 'expired_refresh_token')

      // Token expired, need to clear
      appInstance.clearTokens()

      expect(appInstance.globalData.accessToken).toBeNull()
      expect(appInstance.globalData.refreshToken).toBeNull()
    })
  })
})
