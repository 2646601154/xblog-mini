<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryList, getArticleList } from '@/api'
import type { CategoryVO } from '@/api/modules/article'
import type { ArticleListItemVO } from '@/api/modules/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import AppPagination from '@/components/common/AppPagination.vue'

interface CategoryWithCount extends CategoryVO {}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const categories = ref<CategoryWithCount[]>([])
const articles = ref<ArticleListItemVO[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const currentSlug = computed(() => (route.params.slug as string) || '')

const currentCategory = computed(() => {
  if (!currentSlug.value) return categories.value[0] || null
  return categories.value.find((c) => c.slug === currentSlug.value) || null
})

const hasArticles = computed(() => articles.value.length > 0)

function loadCategories() {
  return getCategoryList().then((res) => {
    categories.value = res.data.data || []
  })
}

async function loadArticles() {
  if (!currentCategory.value) {
    articles.value = []
    pagination.total = 0
    return
  }

  loading.value = true
  try {
    const res = await getArticleList({
      page: pagination.page,
      size: pagination.size,
      categoryId: currentCategory.value.id,
    })
    articles.value = res.data.data.records
    pagination.total = res.data.data.total
  } finally {
    loading.value = false
  }
}

function handleCategoryClick(slug: string) {
  router.push(`/categories/${slug}`)
  pagination.page = 1
}

function handleArticleClick(id: number) {
  router.push(`/article/${id}`)
}

function handlePageChange(page: number) {
  pagination.page = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(currentSlug, () => {
  pagination.page = 1
  loadArticles()
})

onMounted(async () => {
  await loadCategories()
  await loadArticles()
})

import { reactive } from 'vue'
</script>

<template>
  <div class="categories-page">
    <div class="page-header">
      <h1 class="page-title">分类</h1>
    </div>

    <div class="categories-layout">
      <aside class="categories-sidebar">
        <div class="sidebar-card">
          <h3 class="sidebar-title">分类</h3>
          <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
          <ul v-else class="category-list">
            <li
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              :class="{
                active:
                  category.slug === currentSlug || (!currentSlug && category === currentCategory),
              }"
              @click="handleCategoryClick(category.slug)"
            >
              <span class="category-name">{{ category.name }}</span>
            </li>
          </ul>
        </div>
      </aside>

      <main class="categories-main">
        <div v-if="loading" class="article-loading">
          <el-skeleton :rows="3" animated />
          <el-skeleton :rows="3" animated />
        </div>

        <template v-else-if="hasArticles">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
            @click="handleArticleClick"
          />
        </template>

        <el-empty v-else description="该分类下暂无文章" />
      </main>
    </div>

    <AppPagination
      v-if="hasArticles && !loading"
      :current="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      @change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.categories-page {
  margin: 0 auto;
  padding: var(--space-3xl) var(--space-lg);
  min-height: 400px;
}

.page-header {
  margin-bottom: var(--space-3xl);
}

.page-title {
  font-size: var(--text-4xl);
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.categories-layout {
  display: flex;
  gap: var(--space-3xl);
}

.categories-sidebar {
  width: 30%;
  flex-shrink: 0;
}

.sidebar-card {
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-md);
  position: sticky;
  top: calc(var(--header-height) + var(--space-2xl));
  transition:
    box-shadow 0.3s ease,
    transform 0.3s ease;
}

.sidebar-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.sidebar-title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin: 0 0 var(--space-xl) 0;
  padding-bottom: var(--space-md);
  border-bottom: 2px solid var(--color-primary);
  display: inline-block;
  color: var(--text-primary);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-xs);
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.category-item:hover {
  background: var(--bg-elevated);
  color: var(--text-primary);
}

.category-item.active {
  background: var(--color-primary);
  color: var(--bg-surface);
}

.category-item.active .category-name {
  color: var(--bg-surface);
}

.category-item.active :deep(.el-badge__content) {
  background: rgba(255, 255, 255, 0.3);
  color: var(--bg-surface);
}

.category-name {
  font-size: var(--text-base);
}

.categories-main {
  flex: 1;
  min-width: 0;
}

.article-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

@media (max-width: 768px) {
  .categories-layout {
    flex-direction: column;
  }

  .categories-sidebar {
    width: 100%;
  }

  .sidebar-card {
    position: static;
  }

  .page-title {
    font-size: var(--text-2xl);
  }
}
</style>
