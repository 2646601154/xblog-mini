<script setup lang="ts">
import { computed } from 'vue'
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'
import type { ArticleListItemVO } from '@/api'

const props = defineProps<{
  article: ArticleListItemVO
}>()

const emit = defineEmits<{
  click: [id: number]
}>()

const hasCover = computed(() => !!props.article.coverImage)

const coverStyle = computed(() => {
  if (props.article.coverImage) {
    return {
      backgroundImage: `url(${props.article.coverImage})`,
    }
  }
  return {}
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

const estimateReadTime = (summary: string) => {
  const wordsPerMinute = 200
  const words = summary?.length || 0
  const minutes = Math.ceil(words / wordsPerMinute)
  return Math.max(1, minutes)
}
</script>

<template>
  <div class="card-base" @click="handleClick">
    <div class="card-cover" :class="{ 'has-cover': hasCover }" :style="coverStyle">
      <div v-if="!hasCover" class="cover-placeholder"></div>
    </div>
    <div class="card-body">
      <span class="category-tag">{{ article.category.name }}</span>
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="author-row">
          <img class="author-avatar" :src="article.author.avatar" :alt="article.author.nickname" />
          <span class="author-name">{{ article.author.nickname }}</span>
          <span class="separator">·</span>
          <span class="publish-date">{{ formatDate(article.publishedAt) }}</span>
          <span class="separator">·</span>
          <span class="read-time">{{ estimateReadTime(article.summary) }} min read</span>
        </div>
        <div class="stats-row">
          <span class="stat-item">
            <el-icon class="stat-icon"><View /></el-icon>
            <span class="stat-value">{{ formatCount(article.viewCount) }}</span>
          </span>
          <span class="stat-item">
            <el-icon class="stat-icon"><ChatDotRound /></el-icon>
            <span class="stat-value">--</span>
          </span>
          <span class="stat-item">
            <el-icon class="stat-icon"><Star /></el-icon>
            <span class="stat-value">--</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card-base {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  cursor: pointer;
  transition:
    box-shadow 0.3s ease,
    transform 0.3s ease;
}

.card-base:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.card-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: var(--bg-elevated);
  background-size: cover;
  background-position: center;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  overflow: hidden;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--color-primary-100) 0%, var(--color-accent) 100%);
}

.card-body {
  padding: var(--space-xl);
}

.category-tag {
  display: inline-block;
  background: var(--color-primary-100);
  color: var(--color-primary-dark);
  font-size: var(--text-xs);
  font-weight: 500;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-pill);
  margin-bottom: var(--space-md);
}

.card-title {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: var(--space-sm);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--space-lg);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-lg);
  border-top: 1px solid var(--border-light);
}

.author-row {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.author-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: var(--space-xs);
}

.author-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.separator {
  color: var(--text-muted);
  margin: 0 var(--space-xs);
}

.publish-date,
.read-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.stats-row {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.stat-icon {
  font-size: 14px;
}

.stat-value {
  min-width: 20px;
}

@media (max-width: 768px) {
  .card-body {
    padding: var(--space-lg);
  }

  .card-title {
    font-size: var(--text-lg);
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }

  .stats-row {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
