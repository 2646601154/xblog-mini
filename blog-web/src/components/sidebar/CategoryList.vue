<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import type { CategoryVO } from '@/api'

interface CategoryWithCount extends CategoryVO {
}

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
  background: var(--white);
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: 0 2px 12px var(--shadow);
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--accent);
  display: inline-block;
  color: var(--text-primary);
}

.list {
  list-style: none;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-item:hover {
  background: var(--bg-secondary);
  padding-left: 16px;
}

.category-item.active {
  background: var(--accent);
  color: var(--white);
}

.category-item.active .category-name {
  color: var(--white);
}

.category-item.active :deep(.el-badge__content) {
  background: rgba(255, 255, 255, 0.3);
  color: var(--white);
}

.category-name {
  font-size: 0.95rem;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .category-list {
    padding: 16px;
  }
}
</style>
