<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { getArticleDetail, getArticlePrevNext, type ArticleDetailVO } from '@/api/modules/article'
import { getCategoryList, getTagList, type CategoryVO, type TagVO } from '@/api'
import CategoryList from '@/components/sidebar/CategoryList.vue'
import TagCloud from '@/components/sidebar/TagCloud.vue'
import CommentList from '@/components/comment/CommentList.vue'
import CommentForm from '@/components/comment/CommentForm.vue'

interface CategoryWithCount extends CategoryVO {
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref(false)
const article = ref<ArticleDetailVO | null>(null)
const categories = ref<CategoryWithCount[]>([])
const tags = ref<TagVO[]>([])

const prevArticle = ref<{ id: number; title: string } | null>(null)
const nextArticle = ref<{ id: number; title: string } | null>(null)

const commentListRef = ref<InstanceType<typeof CommentList> | null>(null)

// 使用 computed 或 ref 动态获取 articleId，避免组件复用时 id 不更新
const articleId = ref(Number(route.params.id))

async function loadArticleDetail() {
  loading.value = true
  try {
    const res = await getArticleDetail(articleId.value)
    article.value = res.data.data
  } catch {
    ElMessage.error('文章不存在或已被删除')
    router.push('/')
  } finally {
    loading.value = false
  }
}

async function loadSidebarData() {
  const [categoryRes, tagRes] = await Promise.all([
    getCategoryList(),
    getTagList(),
  ])
  categories.value = categoryRes.data.data || []
  tags.value = tagRes.data.data || []
}

async function loadPrevNext() {
  try {
    const res = await getArticlePrevNext(articleId.value)
    const data = res.data.data
    prevArticle.value = data.previous
    nextArticle.value = data.next
  } catch {
    prevArticle.value = null
    nextArticle.value = null
  }
}

async function loadAllData() {
  await Promise.all([
    loadArticleDetail(),
    loadSidebarData(),
    loadPrevNext(),
  ])
}

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId) {
    articleId.value = Number(newId)
    loadAllData()
    // 滚动到顶部
    window.scrollTo(0, 0)
  }
})

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

function formatCount(count: number) {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

async function handleShare() {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

function handleCommentSuccess() {
  commentListRef.value?.reload()
}

function handleTagClick(slug: string) {
  router.push(`/?tag=${slug}`)
}

function handleTagSelect(slug: string) {
  router.push(`/?tag=${slug}`)
}

onMounted(loadAllData)
</script>

<template>
  <div class="article-detail">
    <el-skeleton :rows="10" animated v-if="loading" />

    <template v-else-if="article">
      <el-row :gutter="24">
        <el-col :span="24" :md="16">
          <article class="article-main">
            <div v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </div>

            <header class="article-header">
              <h1 class="article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <div class="meta-author">
                  <el-avatar :size="32" :src="article.author.avatar" class="author-avatar">
                    {{ article.author.nickname?.[0] || 'U' }}
                  </el-avatar>
                  <span class="author-name">{{ article.author.nickname }}</span>
                </div>
                <span class="meta-separator">·</span>
                <span class="meta-date">{{ formatDate(article.publishedAt) }}</span>
                <span class="meta-separator">·</span>
                <span class="meta-views">
                  <el-icon><View /></el-icon>
                  {{ formatCount(article.viewCount) }}
                </span>
              </div>
              <div class="article-tags">
                <span
                  v-for="tag in article.tags"
                  :key="tag.id"
                  class="tag-pill"
                  @click="handleTagClick(tag.slug)"
                >
                  {{ tag.name }}
                </span>
              </div>
            </header>

            <div class="article-content" v-html="article.content"></div>

            <footer class="article-footer">
              <div class="share-section">
                <el-button type="primary" plain size="small" @click="handleShare">
                  复制链接
                </el-button>
              </div>

              <nav class="article-nav" v-if="prevArticle || nextArticle">
                <router-link
                  v-if="prevArticle"
                  :to="`/article/${prevArticle.id}`"
                  class="nav-card nav-prev"
                >
                  <span class="nav-label">上一篇</span>
                  <span class="nav-title">{{ prevArticle.title }}</span>
                </router-link>
                <router-link
                  v-if="nextArticle"
                  :to="`/article/${nextArticle.id}`"
                  class="nav-card nav-next"
                >
                  <span class="nav-label">下一篇</span>
                  <span class="nav-title">{{ nextArticle.title }}</span>
                </router-link>
              </nav>
              <div v-else class="nav-placeholder">
                没有更多文章了
              </div>
            </footer>

            <CommentList ref="commentListRef" :article-id="articleId" />
            <CommentForm :article-id="articleId" @success="handleCommentSuccess" />
          </article>
        </el-col>

        <el-col :span="24" :md="8" class="sidebar-col">
          <CategoryList :categories="categories" />
          <TagCloud :tags="tags" @select="handleTagSelect" />
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style scoped>
.article-detail {
  min-height: 400px;
}

.article-main {
  background: transparent;
}

.article-cover {
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.article-cover img {
  width: 100%;
  display: block;
  border-radius: var(--radius-xl);
}

.article-header {
  margin-bottom: var(--space-xl);
}

.article-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 var(--space-lg) 0;
  color: var(--text-primary);
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.meta-author {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.author-avatar {
  border: 2px solid var(--color-primary-100);
  border-radius: 50%;
}

.author-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.meta-separator {
  color: var(--text-muted);
}

.meta-views {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}

.tag-pill {
  display: inline-block;
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-pill);
  padding: var(--space-xs) var(--space-md);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-pill:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.article-content {
  line-height: 1.8;
  font-size: var(--text-base);
  color: var(--text-primary);
  margin-bottom: var(--space-2xl);
}

.article-content :deep(h2) {
  font-size: var(--text-2xl);
  font-weight: 600;
  margin: var(--space-3xl) 0 var(--space-lg) 0;
  color: var(--text-primary);
}

.article-content :deep(h3) {
  font-size: var(--text-xl);
  font-weight: 600;
  margin: var(--space-2xl) 0 var(--space-md) 0;
  color: var(--text-primary);
}

.article-content :deep(p) {
  margin: 0 0 var(--space-lg) 0;
}

.article-content :deep(pre) {
  background: var(--color-primary-50);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  overflow-x: auto;
  margin: var(--space-lg) 0;
}

.article-content :deep(code) {
  font-family: monospace;
  font-size: 0.9em;
}

.article-content :deep(p) code,
.article-content :deep(li) code {
  background: var(--color-primary-50);
  color: var(--color-primary-dark);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.article-content :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  background: var(--bg-elevated);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  padding: var(--space-lg);
  margin: var(--space-lg) 0;
  color: var(--text-secondary);
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-lg) 0;
}

.article-content :deep(a) {
  color: var(--color-primary);
  text-decoration: none;
}

.article-content :deep(a:hover) {
  text-decoration: underline;
}

.article-footer {
  padding-top: var(--space-xl);
  border-top: 1px solid var(--border-light);
}

.share-section {
  margin-bottom: var(--space-xl);
}

.share-section :deep(.el-button) {
  border-radius: var(--radius-pill);
}

.article-nav {
  display: flex;
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.nav-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  background: var(--bg-elevated);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  text-decoration: none;
  transition: background 0.2s ease;
}

.nav-card:hover {
  background: var(--color-primary-50);
}

.nav-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.nav-title {
  font-size: var(--text-sm);
  color: var(--color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-placeholder {
  color: var(--text-muted);
  font-size: var(--text-sm);
  text-align: center;
  padding: var(--space-lg) 0;
}

.sidebar-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

@media (max-width: 768px) {
  .article-title {
    font-size: var(--text-2xl);
  }

  .article-nav {
    flex-direction: column;
  }

  .sidebar-col {
    margin-top: var(--space-xl);
  }
}
</style>
