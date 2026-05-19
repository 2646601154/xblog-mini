<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getArticleList, getCategoryList, getTagList } from '@/api'
import type { ArticleListItemVO, ArticleListDTO, CategoryVO, TagVO } from '@/api'
import ArticleCard from '@/components/article/ArticleCard.vue'
import CategoryList from '@/components/sidebar/CategoryList.vue'
import TagCloud from '@/components/sidebar/TagCloud.vue'
import AppPagination from '@/components/common/AppPagination.vue'

interface CategoryWithCount extends CategoryVO {
}

const router = useRouter()
const route = useRoute()

const articleLoading = ref(false)
const initLoading = ref(false)
const articles = ref<ArticleListItemVO[]>([])
const categories = ref<CategoryWithCount[]>([])
const tags = ref<TagVO[]>([])

const selectedTagSlug = ref<string | null>(null)

const tagIdMap = computed(() => {
  const map = new Map<string, number>()
  tags.value.forEach(t => map.set(t.slug, t.id))
  return map
})

const selectedTagName = computed(() => {
  if (!selectedTagSlug.value) return ''
  return tags.value.find(t => t.slug === selectedTagSlug.value)?.name || ''
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
  const [categoryRes, tagRes] = await Promise.all([
    getCategoryList(),
    getTagList(),
  ])
  categories.value = categoryRes.data.data || []
  tags.value = tagRes.data.data || []
}

function handleArticleClick(id: number) {
  router.push(`/article/${id}`)
}

function handleTagSelect(slug: string) {
  selectedTagSlug.value = slug
  pagination.page = 1
  loadArticles()
}

function clearTagFilter() {
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
    // 先加载侧边栏数据，确保 tags 列表已准备好
    await loadSidebarData()
    
    // 检查 URL 是否有 tag 参数
    const tagSlug = route.query.tag as string
    if (tagSlug) {
      selectedTagSlug.value = tagSlug
    }
    
    await loadArticles()
  } finally {
    initLoading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <el-row :gutter="20">
      <el-col :span="24" :md="16">
        <template v-if="initLoading">
          <el-skeleton :rows="3" animated />
        </template>

        <template v-else>
          <div class="article-filter">
            <el-breadcrumb separator=">">
              <el-breadcrumb-item :class="{ clickable: selectedTagSlug }" @click="clearTagFilter">
                全部文章
              </el-breadcrumb-item>
              <el-breadcrumb-item v-if="selectedTagName">
                {{ selectedTagName }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <template v-if="articleLoading">
            <el-skeleton :rows="3" animated v-for="i in 4" :key="i" class="skeleton-card" />
          </template>

          <template v-else-if="hasArticles">
            <ArticleCard
              v-for="article in articles"
              :key="article.id"
              :article="article"
              @click="handleArticleClick(article.id)"
            />
          </template>

          <el-empty v-else description="当前没有文章" />

          <AppPagination
            v-if="hasArticles"
            :current="pagination.page"
            :page-size="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </template>
      </el-col>

      <el-col :span="24" :md="8" class="sidebar-col">
        <CategoryList :categories="categories" />
        <TagCloud :tags="tags" @select="handleTagSelect" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 400px;
}

.article-filter {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius);
}

.article-filter :deep(.el-breadcrumb__item) {
  font-size: 0.95rem;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  color: var(--accent);
}

.skeleton-card {
  margin-bottom: 20px;
}

.skeleton-card :deep(.el-card) {
  border-radius: var(--radius);
}

.sidebar-col {
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .sidebar-col {
    margin-top: 20px;
  }
}
</style>
