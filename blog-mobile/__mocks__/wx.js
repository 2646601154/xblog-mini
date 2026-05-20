/**
 * WeChat Mini Program API Mock
 * Provides a comprehensive mock for wx API used in tests
 */

// Storage mock
const storage = new Map()

// Navigation mock
let currentPage = null
const pageStack = []

// Event listeners
const eventListeners = {}

// Mock implementation
const mockWx = {
  // Storage APIs
  getStorageSync: jest.fn((key) => storage.get(key)),
  setStorageSync: jest.fn((key, value) => storage.set(key, value)),
  removeStorageSync: jest.fn((key) => storage.delete(key)),
  clearStorageSync: jest.fn(() => storage.clear()),
  getStorageInfoSync: jest.fn(() => ({
    keys: Array.from(storage.keys()),
    currentSize: storage.size,
    limitSize: 10240
  })),

  // Storage async APIs
  getStorage: jest.fn(({ key, success, fail }) => {
    try {
      const value = storage.get(key)
      success && success({ data: value })
    } catch (e) {
      fail && fail({ errMsg: e.message })
    }
  }),
  setStorage: jest.fn(({ key, data, success, fail }) => {
    try {
      storage.set(key, data)
      success && success()
    } catch (e) {
      fail && fail({ errMsg: e.message })
    }
  }),

  // Request API
  request: jest.fn(),

  // Navigation APIs
  navigateTo: jest.fn(({ url }) => {
    pageStack.push(url)
  }),
  redirectTo: jest.fn(({ url }) => {
    if (pageStack.length > 0) pageStack.pop()
    pageStack.push(url)
  }),
  switchTab: jest.fn(),
  navigateBack: jest.fn((options = {}) => {
    const delta = options.delta || 1
    for (let i = 0; i < delta && pageStack.length > 0; i++) {
      pageStack.pop()
    }
  }),
  reLaunch: jest.fn(),
 EventChannel: jest.fn(),

  // Toast API
  showToast: jest.fn(({ title, icon, duration, mask, success, fail }) => {
    success && success()
  }),
  hideToast: jest.fn(),
  showLoading: jest.fn(),
  hideLoading: jest.fn(),

  // Modal API
  showModal: jest.fn(),
  showActionSheet: jest.fn(),

  // User Info API
  getUserProfile: jest.fn(),

  // Login API
  login: jest.fn(),

  // Page APIs
  getCurrentPages: jest.fn(() => pageStack.map(url => ({ route: url }))),
  Page: jest.fn(),
  Component: jest.fn(),

  // App API
  getApp: jest.fn(),

  // Environment
  canIUse: jest.fn((schema) => true),
  env: {
    VERSION: '2.19.0'
  },

  // Platform
  platform: 'devtools',

  // Update
  updateWeChatApp: jest.fn(),

  // Background
  getBackgroundAudioManager: jest.fn(),

  // Performance
  getPerformance: jest.fn(),

  // Analytics
  reportAnalytics: jest.fn(),

  // Subscribe
  requestSubscribeMessage: jest.fn(),

  // Share
  showShareMenu: jest.fn(),
  hideShareMenu: jest.fn(),

  // Set
  setStorage: jest.fn(),
  getSetting: jest.fn(),
  openSetting: jest.fn(),

  // Event
  onAppShow: jest.fn(),
  offAppShow: jest.fn(),
  onAppHide: jest.fn(),
  offAppHide: jest.fn(),

  // Interceptors
  interceptors: {
    request: {
      use: jest.fn()
    },
    response: {
      use: jest.fn()
    }
  },

  // Cloud
  cloud: jest.fn(),

  // Worker
  createWorker: jest.fn()
}

// Helper functions for tests
mockWx.__resetStorage = () => {
  storage.clear()
  jest.clearAllMocks()
}

mockWx.__addToStorage = (key, value) => {
  storage.set(key, value)
}

mockWx.__getStorage = () => storage

mockWx.__clearPageStack = () => {
  pageStack.length = 0
}

mockWx.__setCurrentPage = (page) => {
  currentPage = page
}

// Default request mock implementation
mockWx.request.mockDefaultResponse = (response) => {
  mockWx.request.mockImplementation((options) => {
    if (options.success) {
      options.success(response)
    }
    return {
      abort: jest.fn()
    }
  })
}

// Mock for successful request
mockWx.request.mockResolvedValue = (data) => {
  mockWx.request.mockImplementation((options) => {
    setTimeout(() => {
      if (options.success) {
        options.success({ statusCode: 200, data: { code: 200, data } })
      }
    }, 0)
    return { abort: jest.fn() }
  })
}

// Mock for failed request
mockWx.request.mockRejectedValue = (error) => {
  mockWx.request.mockImplementation((options) => {
    setTimeout(() => {
      if (options.fail) {
        options.fail(error)
      }
    }, 0)
    return { abort: jest.fn() }
  })
}

global.wx = mockWx

module.exports = mockWx
