<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, getCategoryList, getTagList } from '@/api'
import type { ArticleListItemVO, CategoryVO, TagVO } from '@/api'

interface CategoryWithCount extends CategoryVO {
  articleCount?: number
}
import ArticleCard from '@/components/article/ArticleCard.vue'
import CategoryList from '@/components/sidebar/CategoryList.vue'
import TagCloud from '@/components/sidebar/TagCloud.vue'
import AppPagination from '@/components/common/AppPagination.vue'

const router = useRouter()

// 数据状态
const articleLoading = ref(false)
const initLoading = ref(false)
const articles = ref<ArticleListItemVO[]>([])
const categories = ref<CategoryWithCount[]>([])
const tags = ref<TagVO[]>([])

// 分页状态
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 是否有文章
const hasArticles = computed(() => articles.value.length > 0)

// 加载文章列表
async function loadArticles() {
  articleLoading.value = true
  try {
    const res = await getArticleList({ page: pagination.page, size: pagination.size })
    articles.value = res.data.records
    pagination.total = res.data.total
  } finally {
    articleLoading.value = false
  }
}

// 加载侧边栏数据
async function loadSidebarData() {
  const [categoryRes, tagRes] = await Promise.all([
    getCategoryList(),
    getTagList(),
  ])
  categories.value = categoryRes.data || []
  tags.value = tagRes.data || []
}

// 文章点击跳转
function handleArticleClick(id: number) {
  router.push(`/article/${id}`)
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 初始化
onMounted(async () => {
  initLoading.value = true
  try {
    await Promise.all([loadArticles(), loadSidebarData()])
  } finally {
    initLoading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <el-row :gutter="20">
      <!-- 主内容区 -->
      <el-col :span="24" :md="16">
        <!-- 骨架屏 -->
        <template v-if="initLoading">
          <el-skeleton :rows="3" animated />
        </template>

        <!-- 文章列表 -->
        <template v-else>
          <!-- 加载中显示占位 -->
          <template v-if="articleLoading">
            <el-skeleton :rows="3" animated v-for="i in 4" :key="i" class="skeleton-card" />
          </template>

          <!-- 文章卡片 -->
          <template v-else-if="hasArticles">
            <ArticleCard
              v-for="article in articles"
              :key="article.id"
              :article="article"
              @click="handleArticleClick"
            />
          </template>

          <!-- 空状态 -->
          <el-empty v-else description="当前没有文章" />

          <!-- 分页 -->
          <AppPagination
            v-if="hasArticles"
            :current="pagination.page"
            :page-size="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </template>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="24" :md="8" class="sidebar-col">
        <CategoryList :categories="categories" />
        <TagCloud :tags="tags" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 400px;
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
