<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getArticleComments } from '@/api/modules/comment'
import type { CommentVO } from '@/api/modules/comment'

const props = defineProps<{
  articleId: number
}>()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)

async function loadComments() {
  loading.value = true
  try {
    const res = await getArticleComments(props.articleId)
    comments.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

defineExpose({ reload: loadComments })

onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="comment-list">
    <h3 class="comment-title">评论 ({{ total }})</h3>

    <el-skeleton :rows="2" animated v-if="loading" />

    <div v-else-if="comments.length === 0" class="comment-empty">
      暂无评论，来发表第一条评论吧
    </div>

    <div v-else class="comment-items">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="40" :src="comment.user.avatar">
          {{ comment.user.nickname?.[0] || 'U' }}
        </el-avatar>
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-author">{{ comment.user.nickname }}</span>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <p class="comment-content">{{ comment.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-list {
  margin-top: var(--space-3xl);
  padding-top: var(--space-3xl);
  border-top: 1px solid var(--border-light);
}

.comment-title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: var(--space-xl);
  color: var(--text-primary);
}

.comment-empty {
  text-align: center;
  padding: var(--space-3xl) 0;
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.comment-item {
  display: flex;
  gap: var(--space-md);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-sm);
}

.comment-author {
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--text-base);
}

.comment-date {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.comment-content {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  word-break: break-word;
}
</style>
