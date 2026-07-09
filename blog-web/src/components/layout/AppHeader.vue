<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Sunny, Moon } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import SearchInput from '@/components/common/SearchInput.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const isMenuOpen = ref(false)

const navItems = [
  { label: '首页', path: '/' },
  { label: '归档', path: '/archive' },
  { label: '关于', path: '/about' },
]

const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

const navigateTo = (path: string) => {
  router.push(path)
  isMenuOpen.value = false
}

const handleSearch = (query: string) => {
  router.push(`/?q=${encodeURIComponent(query)}`)
  isMenuOpen.value = false
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    authStore.logout()
    router.push('/')
  }
  isMenuOpen.value = false
}
</script>

<template>
  <header class="header">
    <div class="header-container">
      <div class="logo" @click="navigateTo('/')">
        <img src="/ico/xblog-mini.ico" class="logo-image" />
        blog
      </div>

      <nav class="header-nav">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-link"
          :class="{ 'nav-link--active': isActive(item.path) }">
          {{ item.label }}
        </router-link>
      </nav>

      <div class="header-search">
        <SearchInput @search="handleSearch" />
      </div>

      <div class="header-auth">
        <button class="theme-toggle" @click="themeStore.toggle()" :title="themeStore.theme === 'light' ? '切换暗色模式' : '切换亮色模式'">
          <el-icon :size="18" class="theme-icon" :class="{ 'theme-icon--active': themeStore.theme === 'dark' }">
            <Sunny v-if="themeStore.theme === 'light'" />
            <Moon v-else />
          </el-icon>
        </button>
        <template v-if="authStore.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="authStore.avatar">
                {{ authStore.nickname?.[0] || 'U' }}
              </el-avatar>
              <span class="nickname">{{ authStore.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" round @click="navigateTo('/login')">
            登录
          </el-button>
        </template>
      </div>

      <el-icon class="mobile-menu-icon" :size="24" @click="isMenuOpen = !isMenuOpen">
        <Menu />
      </el-icon>
    </div>

    <el-drawer v-model="isMenuOpen" direction="ttb" :show-close="false" :with-header="false" size="auto">
      <div class="mobile-menu">
        <div class="mobile-nav">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-link"
            :class="{ 'nav-link--active': isActive(item.path) }" @click="isMenuOpen = false">
            {{ item.label }}
          </router-link>
        </div>
        <div class="mobile-search">
          <SearchInput @search="handleSearch" />
        </div>
        <div class="mobile-auth">
          <template v-if="authStore.isLoggedIn">
            <div class="mobile-user-info" @click="navigateTo('/profile')">
              <el-avatar :size="40" :src="authStore.avatar">
                {{ authStore.nickname?.[0] || 'U' }}
              </el-avatar>
              <span class="mobile-nickname">{{ authStore.nickname }}</span>
            </div>
            <el-button plain @click="handleCommand('logout')">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" round @click="navigateTo('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </el-drawer>
  </header>
</template>

<style scoped>
.header {
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.header-container {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--space-xl);
  display: flex;
  align-items: center;
  height: var(--header-height);
  gap: var(--space-lg);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--text-primary);
  font-size: var(--text-xl);
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  margin-right: var(--space-md);
}

.logo-image {
  height: 32px;
  object-fit: contain;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-base);
  color: #fff;
}

.logo-text {
  font-family: 'Georgia', serif;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex: 1;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 500;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: var(--color-primary-50);
  color: var(--color-primary);
}

.nav-link--active {
  background: var(--color-primary-100);
  color: var(--color-primary);
  font-weight: 600;
}

.header-search {
  flex-shrink: 0;
}

.header-auth {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.theme-toggle:hover {
  background: var(--color-primary-50);
  color: var(--color-primary);
}

.theme-icon {
  transition: transform 0.4s ease, opacity 0.3s ease;
}

.theme-icon--active {
  transform: rotate(360deg);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  color: var(--text-primary);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  transition: background-color 0.2s ease;
}

.user-info:hover {
  background: var(--color-primary-50);
}

.nickname {
  font-size: var(--text-sm);
  font-weight: 500;
}

.mobile-menu-icon {
  display: none;
  color: var(--text-primary);
  cursor: pointer;
}

.mobile-menu {
  background: var(--bg-surface);
  padding: var(--space-xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.mobile-nav .nav-link {
  padding: var(--space-md) var(--space-lg);
  font-size: var(--text-base);
}

.mobile-search {
  padding: 0 var(--space-sm);
}

.mobile-auth {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-light);
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--color-primary-50);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.mobile-nickname {
  color: var(--text-primary);
  font-size: var(--text-base);
  font-weight: 500;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 var(--space-lg);
  }

  .header-nav,
  .header-search,
  .header-auth {
    display: none;
  }

  .mobile-menu-icon {
    display: block;
  }
}
</style>