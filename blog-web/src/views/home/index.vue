<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { getArticleList, getCategoryList, getTagList } from '@/api'
import type { ArticleListItemVO, ArticleListDTO, CategoryVO, TagVO } from '@/api'
import ArticleCard from '@/components/article/ArticleCard.vue'
import CategoryList from '@/components/sidebar/CategoryList.vue'
import TagCloud from '@/components/sidebar/TagCloud.vue'
import AboutMe from '@/components/sidebar/AboutMe.vue'
import AppPagination from '@/components/common/AppPagination.vue'

interface CategoryWithCount extends CategoryVO {}

const router = useRouter()
const route = useRoute()

const articleLoading = ref(false)
const initLoading = ref(false)
const articles = ref<ArticleListItemVO[]>([])
const categories = ref<CategoryWithCount[]>([])
const tags = ref<TagVO[]>([])

const selectedTagSlug = ref<string | null>(null)
const searchQuery = ref('')

const tagIdMap = computed(() => {
  const map = new Map<string, number>()
  tags.value.forEach((t) => map.set(t.slug, t.id))
  return map
})

const selectedTagName = computed(() => {
  if (!selectedTagSlug.value) return ''
  return tags.value.find((t) => t.slug === selectedTagSlug.value)?.name || ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const hasArticles = computed(() => articles.value.length > 0)

async function loadArticles() {
  articleLoading.value = true
  try {
    const params: ArticleListDTO = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchQuery.value || undefined,
    }
    if (selectedTagSlug.value) {
      params.tagId = tagIdMap.value.get(selectedTagSlug.value)!
    }
    const res = await getArticleList(params)
    articles.value = res.data.data.records
    pagination.total = res.data.data.total
  } finally {
    articleLoading.value = false
  }
}

async function loadSidebarData() {
  const [categoryRes, tagRes] = await Promise.all([getCategoryList(), getTagList()])
  categories.value = categoryRes.data.data || []
  tags.value = tagRes.data.data || []
}

function handleArticleClick(id: number) {
  router.push(`/article/${id}`)
}

function handleTagSelect(slug: string) {
  selectedTagSlug.value = slug
  searchQuery.value = ''
  pagination.page = 1
  router.push({ query: slug ? { tag: slug } : {} })
  loadArticles()
}

function clearTagFilter() {
  selectedTagSlug.value = null
  searchQuery.value = ''
  pagination.page = 1
  router.push('/')
  loadArticles()
}

function clearSearch() {
  searchQuery.value = ''
  selectedTagSlug.value = null
  pagination.page = 1
  router.push('/')
  loadArticles()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  initLoading.value = true
  try {
    await loadSidebarData()

    const tagSlug = route.query.tag as string
    if (tagSlug) {
      selectedTagSlug.value = tagSlug
    }

    const q = route.query.q as string
    if (q) {
      searchQuery.value = q
    }

    await loadArticles()
  } finally {
    initLoading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <!-- Gradient Banner -->
    <div class="gradient-banner">
      <h1 class="banner-title">欢迎来到我的博客</h1>
      <p class="banner-subtitle">分享技术心得与生活感悟</p>
    </div>

    <!-- Two-column Layout -->
    <el-row :gutter="24" class="main-layout">
      <!-- Main Content -->
      <el-col :span="24" :md="16" class="main-col">
        <!-- Article Filter -->
        <div class="article-filter">
          <span class="filter-label">全部文章</span>
          <span v-if="selectedTagName" class="filter-separator">/</span>
          <span v-if="selectedTagName" class="filter-tag">{{ selectedTagName }}</span>
          <span v-if="selectedTagName" class="clear-btn" @click="clearTagFilter">
            <el-icon>
              <Close />
            </el-icon>
          </span>
          <span v-if="searchQuery" class="filter-separator">/</span>
          <span v-if="searchQuery" class="filter-tag">{{ searchQuery }}</span>
          <span v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <el-icon>
              <Close />
            </el-icon>
          </span>
        </div>

        <!-- Article Loading -->
        <template v-if="articleLoading">
          <el-skeleton :rows="3" animated v-for="i in 4" :key="i" class="skeleton-card" />
        </template>

        <!-- Article List -->
        <template v-else-if="hasArticles">
          <div class="article-list-section">
            <ArticleCard
              v-for="article in articles"
              :key="article.id"
              :article="article"
              @click="handleArticleClick(article.id)"
            />
          </div>
          <AppPagination
            :current="pagination.page"
            :page-size="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </template>

        <!-- Empty State -->
        <el-empty v-else description="当前没有文章" />
      </el-col>

      <!-- Sidebar -->
      <el-col :span="24" :md="8" class="sidebar-col">
        <div class="sidebar-sticky">
          <AboutMe class="sidebar-widget" />
          <CategoryList :categories="categories" class="sidebar-widget" />
          <TagCloud :tags="tags" @select="handleTagSelect" class="sidebar-widget" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 400px;
  padding-bottom: var(--space-5xl);
}

.container {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: var(--space-xl) var(--space-lg);
}

/* Banner */
.gradient-banner {
  border-radius: var(--radius-xl);
  padding: var(--space-3xl);
  margin-bottom: var(--space-3xl);
  text-align: center;
}

.banner-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  margin: 0 0 var(--space-md) 0;
  color: var(--text-primary);
}

.banner-subtitle {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-xl) 0;
}

.cta-btn {
  background: var(--color-primary);
  color: white;
  font-weight: 600;
  border: none;
  padding: var(--space-md) var(--space-2xl);
}

.cta-btn:hover {
  background: var(--color-primary-dark);
}

/* Main Layout */
.main-layout {
  align-items: flex-start;
}

.main-col {
  min-width: 0;
}

/* Article Filter */
.article-filter {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  background: var(--bg-elevated);
  border-radius: var(--radius-pill);
  padding: var(--space-sm) var(--space-lg);
  margin-bottom: var(--space-xl);
}

.filter-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.filter-separator {
  color: var(--text-muted);
}

.filter-tag {
  font-size: var(--text-sm);
  color: var(--color-primary);
  font-weight: 500;
}

.clear-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-primary-100);
  color: var(--color-primary);
  cursor: pointer;
  margin-left: var(--space-xs);
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: var(--color-primary);
  color: white;
}

/* Article List */
.article-list-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.skeleton-card {
  margin-bottom: var(--space-xl);
}

.skeleton-card :deep(.el-card) {
  border-radius: var(--radius-lg);
}

/* Sidebar */
.sidebar-col {
  min-width: 0;
}

.sidebar-sticky {
  position: sticky;
  top: calc(var(--header-height) + var(--space-lg));
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.sidebar-widget {
  width: 100%;
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    padding: var(--space-lg) var(--space-md);
  }

  .gradient-banner {
    padding: var(--space-2xl) var(--space-xl);
    margin-bottom: var(--space-2xl);
  }

  .banner-title {
    font-size: var(--text-2xl);
  }

  .banner-subtitle {
    font-size: var(--text-sm);
  }

  .main-layout {
    flex-direction: column;
  }

  .main-col {
    width: 100%;
  }

  .sidebar-col {
    width: 100%;
    margin-top: var(--space-xl);
  }

  .sidebar-sticky {
    position: static;
  }
}
</style>
