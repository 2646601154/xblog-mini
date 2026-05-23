import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

type Theme = 'light' | 'dark'

const THEME_KEY = 'xblog_theme'

function applyTheme(theme: Theme) {
  document.documentElement.setAttribute('data-theme', theme)
  if (theme === 'dark') {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

function getSystemPreference(): Theme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<Theme>('light')

  function toggle() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  function init() {
    const saved = localStorage.getItem(THEME_KEY) as Theme | null
    theme.value = saved ?? getSystemPreference()
    applyTheme(theme.value)
  }

  // 持久化 + 应用
  watch(theme, (val) => {
    localStorage.setItem(THEME_KEY, val)
    applyTheme(val)
  })

  // 监听系统偏好变化（仅在用户未手动设置时跟随）
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', (e) => {
    const saved = localStorage.getItem(THEME_KEY)
    if (!saved) {
      theme.value = e.matches ? 'dark' : 'light'
    }
  })

  return { theme, toggle, init }
})
