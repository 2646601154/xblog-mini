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
      <el-row :gutter="20">
        <el-col :span="24" :md="16">
          <article class="article-main">
            <el-card v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </el-card>

            <header class="article-header">
              <h1 class="article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <div class="meta-author">
                  <el-avatar :size="32" :src="article.author.avatar">
                    {{ article.author.nickname?.[0] || 'U' }}
                  </el-avatar>
                  <span>{{ article.author.nickname }}</span>
                </div>
                <span class="meta-divider">|</span>
                <span class="meta-date">{{ formatDate(article.publishedAt) }}</span>
                <span class="meta-divider">|</span>
                <span class="meta-views">
                  <el-icon><View /></el-icon>
                  {{ formatCount(article.viewCount) }}
                </span>
              </div>
              <div class="article-tags">
                <el-tag
                  v-for="tag in article.tags"
                  :key="tag.id"
                  size="small"
                  class="tag-clickable"
                  @click="handleTagClick(tag.slug)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </header>

            <div class="article-content" v-html="article.content"></div>

            <footer class="article-footer">
              <div class="share-section">
                <span class="share-label">分享文章：</span>
                <el-button size="small" @click="handleShare">
                  复制链接
                </el-button>
              </div>

              <nav class="article-nav">
                <div class="nav-item nav-prev" v-if="prevArticle">
                  <span class="nav-hint">上一篇：</span>
                  <router-link :to="`/article/${prevArticle.id}`" class="nav-link">
                    {{ prevArticle.title }}
                  </router-link>
                </div>
                <div class="nav-item nav-next" v-if="nextArticle">
                  <span class="nav-hint">下一篇：</span>
                  <router-link :to="`/article/${nextArticle.id}`" class="nav-link">
                    {{ nextArticle.title }}
                  </router-link>
                </div>
                <div v-if="!prevArticle && !nextArticle" class="nav-placeholder">
                  没有更多文章了
                </div>
              </nav>
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
  border-radius: var(--radius);
  margin-bottom: 24px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  display: block;
}

.article-header {
  margin-bottom: 30px;
}

.article-title {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 16px 0;
  color: var(--text-primary);
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.meta-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-divider {
  color: var(--border);
}

.meta-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-clickable {
  cursor: pointer;
}

.tag-clickable:hover {
  color: var(--accent);
}

.article-content {
  line-height: 1.8;
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 40px;
}

.article-content :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 24px 0 16px 0;
}

.article-content :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 20px 0 12px 0;
}

.article-content :deep(p) {
  margin: 0 0 16px 0;
}

.article-content :deep(pre) {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: var(--radius);
  overflow-x: auto;
  margin: 16px 0;
}

.article-content :deep(code) {
  font-family: monospace;
  font-size: 0.9em;
}

.article-content :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  padding: 12px 16px;
  margin: 16px 0;
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius);
}

.article-content :deep(a) {
  color: var(--color-primary);
  text-decoration: none;
}

.article-content :deep(a:hover) {
  text-decoration: underline;
}

.article-footer {
  padding-top: 30px;
  border-top: 1px solid var(--border);
}

.share-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}

.share-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.article-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.nav-hint {
  color: var(--text-secondary);
  font-size: 0.9rem;
  flex-shrink: 0;
}

.nav-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.9rem;
}

.nav-link:hover {
  text-decoration: underline;
}

.nav-placeholder {
  color: var(--text-secondary);
  font-size: 0.9rem;
  text-align: center;
  padding: 10px 0;
}

.sidebar-col {
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .article-title {
    font-size: 1.5rem;
  }

  .sidebar-col {
    margin-top: 30px;
  }
}
</style>
