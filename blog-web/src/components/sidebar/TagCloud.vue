<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { TagVO } from '@/api'

const props = defineProps<{
  tags: TagVO[]
}>()

const handleClick = (slug: string) => {
  // TODO: 实现标签页面跳转
  ElMessage.info('标签页面开发中')
  console.log('跳转至标签页面:', slug)
}
</script>

<template>
  <div class="tag-cloud">
    <h3 class="sidebar-title">标签云</h3>
    <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
    <div v-else class="tag-list">
      <el-tag
        v-for="tag in tags"
        :key="tag.id"
        class="tag-item"
        type="info"
        effect="plain"
        @click="handleClick(tag.slug)"
      >
        {{ tag.name }}
      </el-tag>
    </div>
  </div>
</template>

<style scoped>
.tag-cloud {
  background: var(--white);
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: 0 2px 12px var(--shadow);
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

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  color: var(--accent);
  background-color: var(--accent-light);
  border-color: var(--accent);
}

@media (max-width: 768px) {
  .tag-cloud {
    padding: 16px;
  }
}
</style>
