<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)

const menuItems = [
  { path: '/admin', icon: 'Odometer', title: '仪表盘' },
  { path: '/admin/users', icon: 'User', title: '用户管理' },
  { path: '/admin/articles', icon: 'Document', title: '文章管理' },
  { path: '/admin/categories', icon: 'FolderOpened', title: '分类管理' },
  { path: '/admin/tags', icon: 'PriceTag', title: '标签管理' },
  { path: '/admin/comments', icon: 'ChatDotSquare', title: '评论管理' },
  { path: '/admin/config', icon: 'Setting', title: '系统配置' },
  { path: '/admin/media', icon: 'Picture', title: '图片管理' },
]

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function handleMenuSelect(path: string) {
  router.push(path)
}
</script>

<template>
  <el-menu
    :default-active="route.path"
    :collapse="isCollapsed"
    class="h-full"
    @select="handleMenuSelect"
  >
    <div class="p-4 text-center border-b border-gray-200">
      <h1 v-if="!isCollapsed" class="text-lg font-bold text-blue-500">Xblog Admin</h1>
      <span v-else class="text-blue-500 font-bold">X</span>
    </div>
    <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
      <el-icon><component :is="item.icon" /></el-icon>
      <template #title>{{ item.title }}</template>
    </el-menu-item>
  </el-menu>
</template>
