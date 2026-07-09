<script setup lang="ts">
import type { TagVO } from '@/api'

defineProps<{
  tags: TagVO[]
}>()

const emit = defineEmits<{
  select: [slug: string]
}>()

const handleClick = (slug: string) => {
  emit('select', slug)
}
</script>

<template>
  <div class="tag-cloud">
    <h3 class="sidebar-title">标签云</h3>
    <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
    <div v-else class="tag-list">
      <span v-for="tag in tags" :key="tag.id" class="tag-item" @click="handleClick(tag.slug)">
        {{ tag.name }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.tag-cloud {
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: var(--space-xl);
  box-shadow: var(--shadow-md);
}

.sidebar-title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: var(--space-xl);
  color: var(--text-primary);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-medium);
  background: transparent;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.tag-item.selected {
  border: 2px solid var(--text-primary);
  background: var(--color-primary-100);
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .tag-cloud {
    padding: var(--space-lg);
  }
}
</style>
