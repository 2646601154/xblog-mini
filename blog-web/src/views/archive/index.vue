<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/modules/article'
import type { ArticleListItemVO, ArticleListDTO } from '@/api/modules/article'
import { ElSkeleton, ElEmpty } from 'element-plus'

interface YearGroup {
  year: number
  months: Map<number, ArticleListItemVO[]>
}

const router = useRouter()

const loading = ref(false)
const articles = ref<ArticleListItemVO[]>([])

const groupedArticles = computed<YearGroup[]>(() => {
  const yearMap = new Map<number, Map<number, ArticleListItemVO[]>>()

  articles.value.forEach((article) => {
    if (!article.publishedAt) return

    const date = new Date(article.publishedAt)
    const year = date.getFullYear()
    const month = date.getMonth() + 1

    if (!yearMap.has(year)) {
      yearMap.set(year, new Map())
    }
    const monthMap = yearMap.get(year)!
    if (!monthMap.has(month)) {
      monthMap.set(month, [])
    }
    monthMap.get(month)!.push(article)
  })

  const result: YearGroup[] = []
  const sortedYears = Array.from(yearMap.keys()).sort((a, b) => b - a)

  sortedYears.forEach((year) => {
    const monthMap = yearMap.get(year)!
    const sortedMonths = Array.from(monthMap.keys()).sort((a, b) => b - a)
    const months = new Map<number, ArticleListItemVO[]>()
    sortedMonths.forEach((month) => {
      const monthArticles = monthMap.get(month)!
      // 按发布日期排序（新的在前）
      monthArticles.sort((a, b) => {
        if (!a.publishedAt || !b.publishedAt) return 0
        return new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
      })
      months.set(month, monthArticles)
    })
    result.push({ year, months })
  })

  return result
})

const expandedYears = ref<Set<number>>(new Set())

function toggleYear(year: number) {
  if (expandedYears.value.has(year)) {
    expandedYears.value.delete(year)
  } else {
    expandedYears.value.add(year)
  }
}

function isYearExpanded(year: number): boolean {
  return expandedYears.value.has(year)
}

function formatMonth(month: number): string {
  return `${month}月`
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function navigateToArticle(id: number) {
  router.push(`/article/${id}`)
}

async function loadArticles() {
  loading.value = true
  try {
    const params: ArticleListDTO = { page: 1, size: 100 }
    const res = await getArticleList(params)
    articles.value = res.data.data.records || []

    // Expand all years by default
    groupedArticles.value.forEach((group) => {
      expandedYears.value.add(group.year)
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div class="archive-page">
    <h1 class="page-title">文章归档</h1>

    <template v-if="loading">
      <div class="skeleton-group">
        <el-skeleton :rows="2" animated />
        <el-skeleton :rows="3" animated style="margin-top: 24px" />
        <el-skeleton :rows="2" animated style="margin-top: 24px" />
      </div>
    </template>

    <template v-else-if="articles.length === 0">
      <el-empty description="暂无文章" />
    </template>

    <template v-else>
      <div class="archive-list">
        <div v-for="yearGroup in groupedArticles" :key="yearGroup.year" class="year-group">
          <div class="year-header" @click="toggleYear(yearGroup.year)">
            <span class="year-title">{{ yearGroup.year }}年</span>
            <span class="article-count">({{ yearGroup.months.size }}个月)</span>
            <svg
              class="chevron"
              :class="{ expanded: isYearExpanded(yearGroup.year) }"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M6 9l6 6 6-6" />
            </svg>
          </div>

          <div v-show="isYearExpanded(yearGroup.year)" class="year-content">
            <div
              v-for="[month, monthArticles] in yearGroup.months"
              :key="month"
              class="month-group"
            >
              <div class="month-title">{{ formatMonth(month) }}</div>
              <div class="article-list">
                <div
                  v-for="article in monthArticles"
                  :key="article.id"
                  class="article-item"
                  @click="navigateToArticle(article.id)"
                >
                  <span class="article-title">{{ article.title }}</span>
                  <span v-if="article.category" class="category-pill">
                    {{ article.category.name }}
                  </span>
                  <span class="article-date">{{ formatDate(article.publishedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.archive-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-5xl) var(--space-lg);
}

.page-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-3xl);
}

.skeleton-group {
  padding: var(--space-lg);
}

.archive-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.year-group {
  border-left: 3px solid var(--color-primary-100);
  padding-left: var(--space-lg);
}

.year-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  user-select: none;
  padding: var(--space-sm) 0;
}

.year-header:hover .year-title {
  color: var(--color-primary);
}

.year-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  transition: color 0.2s ease;
}

.article-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.chevron {
  width: 20px;
  height: 20px;
  color: var(--text-muted);
  transition: transform 0.2s ease;
  margin-left: auto;
}

.chevron.expanded {
  transform: rotate(180deg);
}

.year-content {
  padding-top: var(--space-md);
}

.month-group {
  margin-bottom: var(--space-xl);
}

.month-title {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: var(--space-md);
}

.article-list {
  display: flex;
  flex-direction: column;
}

.article-item {
  display: flex;
  align-items: center;
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
}

.article-item:hover {
  background: var(--color-primary-50);
  padding-left: var(--space-sm);
  padding-right: var(--space-sm);
}

.article-item:last-child {
  border-bottom: none;
}

.article-title {
  flex: 1;
  font-size: var(--text-base);
  color: var(--text-primary);
  transition: color 0.2s ease;
}

.article-item:hover .article-title {
  color: var(--color-primary);
}

.category-pill {
  font-size: var(--text-xs);
  background: var(--color-primary-100);
  color: var(--color-primary-dark);
  border-radius: var(--radius-pill);
  padding: var(--space-xs) var(--space-md);
  margin: 0 var(--space-lg);
  white-space: nowrap;
}

.article-date {
  font-size: var(--text-sm);
  color: var(--text-muted);
  min-width: 90px;
  text-align: right;
}

@media (max-width: 640px) {
  .archive-page {
    padding: var(--space-3xl) var(--space-md);
  }

  .article-item {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }

  .article-title {
    width: 100%;
    order: 1;
  }

  .category-pill {
    order: 2;
    margin-left: 0;
  }

  .article-date {
    order: 3;
    text-align: left;
  }
}
</style>
