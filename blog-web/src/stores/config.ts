import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPublicConfig } from '@/api'
import type { SiteConfig } from '@/types'

export const useConfigStore = defineStore('config', () => {
  // State
  const config = ref<SiteConfig | null>(null)
  const loaded = ref(false)

  // Getters
  const icpNumber = computed(() => config.value?.icpNumber ?? '')
  const copyright = computed(() => config.value?.copyright ?? '')

  // Actions
  async function fetchConfig(): Promise<void> {
    if (loaded.value) return
    try {
      const res = await getPublicConfig()
      config.value = res.data.data
    } catch {
      // 静默失败，组件中使用 getter 时自动走默认值
    } finally {
      loaded.value = true
    }
  }

  function initConfig(): void {
    fetchConfig()
  }

  return {
    // State
    config,
    // Getters
    icpNumber,
    copyright,
    // Actions
    fetchConfig,
    initConfig,
  }
})
