<script setup lang="ts">
import { computed } from 'vue'
import { View } from '@element-plus/icons-vue'
import type { ArticleListItemVO } from '@/api'

const props = defineProps<{
  article: ArticleListItemVO
}>()

const emit = defineEmits<{
  click: [id: number]
}>()

const coverStyle = computed(() => {
  if (props.article.coverImage) return {}
  return { backgroundColor: 'var(--bg-secondary)' }
})

const handleClick = () => {
  emit('click', props.article.id)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

const formatCount = (count: number) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}
</script>

<template>
  <el-card class="article-card" shadow="hover" @click="handleClick">
    <div class="article-cover" :style="coverStyle">
      <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
    </div>
    <div class="article-body">
      <div class="article-meta">
        <el-tag size="small" type="info">{{ article.category.name }}</el-tag>
        <span class="article-date">{{ formatDate(article.publishedAt) }}</span>
      </div>
      <h3 class="article-title">{{ article.title }}</h3>
      <p class="article-summary">{{ article.summary }}</p>
      <div class="article-footer">
        <div class="article-author">
          <el-avatar :size="32" :src="article.author.avatar">
            {{ article.author.nickname?.[0] || 'U' }}
          </el-avatar>
          <span class="author-name">{{ article.author.nickname }}</span>
        </div>
        <div class="article-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ formatCount(article.viewCount) }}
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.article-card {
  cursor: pointer;
  border-radius: var(--radius);
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-4px);
}

.article-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: var(--radius) var(--radius) 0 0;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-body {
  padding: 20px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.article-date {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.article-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-primary);
}

.article-summary {
  font-size: 0.95rem;
  color: var(--text-secondary);
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.article-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-size: 0.9rem;
  color: var(--text-primary);
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .article-cover {
    height: 160px;
  }

  .article-body {
    padding: 16px;
  }

  .article-title {
    font-size: 1.1rem;
  }
}
</style>
