<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const configStore = useConfigStore()
const isMenuOpen = ref(false)

onMounted(() => {
  configStore.initConfig()
})

const menuItems = [
  { label: '首页', path: '/', key: 'home' },
  { label: '分类', path: '/categories', key: 'category' },
]

const navigateTo = (path: string) => {
  router.push(path)
  isMenuOpen.value = false
}

const handleMenuSelect = (path: string) => {
  navigateTo(path)
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
        <img
          v-if="configStore.siteLogo"
          :src="configStore.siteLogo"
          :alt="configStore.siteTitle"
          class="logo-image"
        />
        <template v-else>
          <span class="logo-icon">{{ configStore.siteTitle[0] }}</span>
          <span class="logo-text">{{ configStore.siteTitle }}</span>
        </template>
      </div>

      <el-menu
        mode="horizontal"
        class="header-menu"
        :ellipsis="false"
        :default-active="$route.path"
        router
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.key"
          :index="item.path"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>

      <div class="header-auth">
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
          <el-button plain @click="navigateTo('/login')">登录</el-button>
          <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
        </template>
      </div>

      <el-icon class="mobile-menu-icon" :size="24" @click="isMenuOpen = !isMenuOpen">
        <Menu />
      </el-icon>
    </div>

    <el-drawer
      v-model="isMenuOpen"
      direction="ttb"
      :show-close="false"
      :with-header="false"
      size="auto"
    >
      <div class="mobile-menu">
        <el-menu
          :default-active="$route.path"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.key"
            :index="item.path"
          >
            {{ item.label }}
          </el-menu-item>
        </el-menu>
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
            <el-button plain @click="navigateTo('/login')">登录</el-button>
            <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-drawer>
  </header>
</template>

<style scoped>
.header {
  background: var(--bg-dark);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 20px var(--shadow);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
  font-size: 1.4rem;
  font-weight: 600;
  cursor: pointer;
  margin-right: 40px;
  flex-shrink: 0;
}

.logo-image {
  height: 36px;
  object-fit: contain;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--accent), var(--accent-light));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.logo-text {
  font-family: 'Georgia', serif;
}

.header-menu {
  flex: 1;
  display: flex;
  align-items: center;
  background: transparent !important;
  height: 64px;
  overflow: hidden;
}

.header-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  margin: 0 4px;
  padding: 0 12px;
}

.header-menu .el-menu-item:hover {
  background-color: rgba(58, 158, 150, 0.1) !important;
}

.header-menu .el-menu-item.is-active {
  background-color: rgba(58, 158, 150, 0.15) !important;
  color: var(--accent) !important;
  border-bottom: none !important;
}

.header-auth {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 24px;
  flex-shrink: 0;
}

.header-auth .el-button {
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-primary);
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(58, 158, 150, 0.1);
}

.nickname {
  font-size: 14px;
}

.mobile-menu-icon {
  display: none;
  color: var(--text-primary);
  cursor: pointer;
}

.mobile-menu {
  background: var(--bg-primary);
  padding: 20px;
}

.mobile-menu .el-menu {
  background: transparent !important;
}

.mobile-menu .el-menu-item {
  color: var(--text-primary) !important;
  font-size: 16px;
  height: 44px;
  line-height: 44px;
}

.mobile-menu .el-menu-item.is-active {
  background-color: rgba(58, 158, 150, 0.1) !important;
  color: var(--accent) !important;
}

.mobile-auth {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 20px;
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(58, 158, 150, 0.08);
  border-radius: 8px;
  cursor: pointer;
}

.mobile-nickname {
  color: var(--text-primary);
  font-size: 16px;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .header-menu {
    display: none;
  }

  .header-auth {
    display: none;
  }

  .mobile-menu-icon {
    display: block;
  }
}
</style>
