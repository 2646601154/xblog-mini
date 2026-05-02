<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const isMenuOpen = ref(false)

const menuItems = [
  { label: '首页', path: '/', key: 'home' },
  { label: '分类', path: '/category/frontend', key: 'category' },
  { label: '标签', path: '/tag/vue', key: 'tag' },
]

const navigateTo = (path: string) => {
  router.push(path)
  isMenuOpen.value = false
}

const handleMenuSelect = (path: string) => {
  navigateTo(path)
}
</script>

<template>
  <header class="header">
    <div class="header-container">
      <div class="logo" @click="navigateTo('/')">
        <span class="logo-icon">📝</span>
        <span class="logo-text">Xblog</span>
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
        <el-button plain @click="navigateTo('/login')">登录</el-button>
        <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
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
          <el-button plain @click="navigateTo('/login')">登录</el-button>
          <el-button type="primary" @click="navigateTo('/register')">注册</el-button>
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
  color: #fff;
  font-size: 1.4rem;
  font-weight: 600;
  cursor: pointer;
  margin-right: 40px;
  flex-shrink: 0;
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
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.header-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
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

.mobile-menu-icon {
  display: none;
  color: #fff;
  cursor: pointer;
}

.mobile-menu {
  background: var(--bg-dark);
  padding: 20px;
}

.mobile-menu .el-menu {
  background: transparent !important;
}

.mobile-menu .el-menu-item {
  color: #fff !important;
  font-size: 16px;
  height: 44px;
  line-height: 44px;
}

.mobile-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
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
