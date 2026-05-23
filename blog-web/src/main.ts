import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIcons from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useConfigStore } from './stores/config'
import { useThemeStore } from './stores/theme'
import '@/styles/main.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIcons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

const authStore = useAuthStore()
authStore.initAuth()

const configStore = useConfigStore()
configStore.initConfig()

const themeStore = useThemeStore()
themeStore.init()

app.mount('#app')
