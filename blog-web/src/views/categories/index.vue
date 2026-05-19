<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryList, getArticleList } from '@/api'
import type { CategoryVO } from '@/api/modules/article'
import type { ArticleListItemVO } from '@/api/modules/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import AppPagination from '@/components/common/AppPagination.vue'

interface CategoryWithCount extends CategoryVO {
}

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

const currentSlug = computed(() => route.params.slug as string || '')

const currentCategory = computed(() => {
  if (!currentSlug.value) return categories.value[0] || null
  return categories.value.find(c => c.slug === currentSlug.value) || null
})

const hasArticles = computed(() => articles.value.length > 0)

function loadCategories() {
  return getCategoryList().then(res => {
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
              :class="{ active: category.slug === currentSlug || (!currentSlug && category === currentCategory) }"
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: 400px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.categories-layout {
  display: flex;
  gap: 30px;
}

.categories-sidebar {
  width: 30%;
  flex-shrink: 0;
}

.sidebar-card {
  background: var(--white);
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: 0 2px 12px var(--shadow);
  position: sticky;
  top: 80px;
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--accent);
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
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-primary);
}

.category-item:hover {
  background: var(--bg-secondary);
}

.category-item.active {
  background: var(--accent);
  color: var(--white);
}

.category-item.active .category-name {
  color: var(--white);
}

.category-item.active :deep(.el-badge__content) {
  background: rgba(255, 255, 255, 0.3);
  color: var(--white);
}

.category-name {
  font-size: 0.95rem;
}

.categories-main {
  flex: 1;
  min-width: 0;
}

.article-loading {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
    font-size: 1.5rem;
  }
}
</style>
