<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { CategoryVO } from '@/api'

interface CategoryWithCount extends CategoryVO {
  articleCount?: number
}

defineProps<{
  categories: CategoryWithCount[]
}>()

const handleClick = (slug: string) => {
  // TODO: 实现分类页面跳转
  ElMessage.info('分类页面开发中')
  console.log('跳转至分类页面:', slug)
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
        @click="handleClick(category.slug)"
      >
        <span class="category-name">{{ category.name }}</span>
        <el-badge :value="category.articleCount ?? 0" :max="99" type="primary" />
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
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-item:last-child {
  border-bottom: none;
}

.category-item:hover {
  color: var(--accent);
  padding-left: 8px;
}

.category-name {
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .category-list {
    padding: 16px;
  }
}
</style>
