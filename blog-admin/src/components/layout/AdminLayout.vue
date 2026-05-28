<script setup lang="ts">
import { ref } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import AdminHeader from './AdminHeader.vue'

const isCollapsed = ref(false)

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <el-container class="h-screen">
    <el-aside :width="isCollapsed ? '64px' : '200px'" class="transition-width duration-300">
      <AdminSidebar />
    </el-aside>
    <el-container>
      <el-header class="!h-14 !p-0">
        <AdminHeader />
      </el-header>
      <el-main class="bg-gray-50">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
