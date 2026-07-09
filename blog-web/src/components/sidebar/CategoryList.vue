<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import type { CategoryVO } from '@/api'

interface CategoryWithCount extends CategoryVO {}

defineProps<{
  categories: CategoryWithCount[]
}>()

const router = useRouter()
const route = useRoute()

const handleClick = (slug: string) => {
  router.push(`/categories/${slug}`)
}

const isActive = (slug: string) => {
  return route.params.slug === slug
}
</script>

<template>
  <div class="category-list">
    <h3 class="sidebar-title">分类</h3>
    <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
    <ul v-else class="list">
      <li
        v-for="category in categories"
        :key="category.id"
        class="category-item"
        :class="{ active: isActive(category.slug) }"
        @click="handleClick(category.slug)"
      >
        <span class="category-name">{{ category.name }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.category-list {
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: var(--space-xl);
  box-shadow: var(--shadow-md);
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: var(--space-xl);
  color: var(--text-primary);
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  display: inline-flex;
  align-items: center;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-xs);
  margin-right: var(--space-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: transparent;
}

.category-item:hover {
  background: var(--color-primary-50);
  color: var(--color-primary);
}

.category-item.active {
  background: var(--color-primary);
  color: white;
}

.category-item.active .category-name {
  color: white;
}

.category-name {
  font-size: var(--text-sm);
  color: inherit;
}

@media (max-width: 768px) {
  .category-list {
    padding: var(--space-lg);
  }
}
</style>
