<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'

const route = useRoute()
const isBlankLayout = computed(() => route.meta?.layout === 'blank')
</script>

<template>
  <AppLayout v-if="!isBlankLayout">
    <router-view />
  </AppLayout>
  <router-view v-else />
</template>
