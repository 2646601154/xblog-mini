<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, ChatDotSquare, FolderOpened } from '@element-plus/icons-vue'
import { getArticleList } from '@/api'
import { getCategoryList } from '@/api'
import { getTagList } from '@/api'
import { getCommentList } from '@/api'
import { getUserList } from '@/api'
import DashboardCharts from '@/components/charts/DashboardCharts.vue'

const router = useRouter()

const stats = ref({
  articleCount: 0,
  userCount: 0,
  commentCount: 0,
  categoryCount: 0,
  tagCount: 0,
})
const loading = ref(true)

// 图表数据
const articleTrend = ref({
  dates: [] as string[],
  counts: [] as number[],
})
const categoryDistribution = ref<{ name: string; value: number }[]>([])
const tagUsage = ref({
  tags: [] as string[],
  counts: [] as number[],
})

async function fetchStats() {
  try {
    const [articles, categories, tags, comments, users] = await Promise.all([
      getArticleList({ page: 1, size: 1 }),
      getCategoryList(),
      getTagList(),
      getCommentList({ page: 1, size: 1 }),
      getUserList({ page: 1, size: 1 }),
    ])

    stats.value = {
      articleCount: articles.data.data.total,
      userCount: users.data.data.total,
      commentCount: comments.data.data.total,
      categoryCount: categories.data.data.length,
      tagCount: tags.data.data.length,
    }

    // 处理分类分布数据
    if (categories.data.data && categories.data.data.length > 0) {
      categoryDistribution.value = categories.data.data.map((cat: any) => ({
        name: cat.name || cat.categoryName || '未分类',
        value: cat.articleCount || Math.floor(Math.random() * 50) + 1, // 临时模拟数据
      }))
    }

    // 处理标签使用数据
    if (tags.data.data && tags.data.data.length > 0) {
      const tagList = tags.data.data.slice(0, 10) // 取前10个标签
      tagUsage.value = {
        tags: tagList.map((tag: any) => tag.name || tag.tagName || ''),
        counts: tagList.map(() => Math.floor(Math.random() * 30) + 1), // 临时模拟数据
      }
    }

    // 生成文章趋势数据（模拟最近7天）
    const dates: string[] = []
    const counts: number[] = []
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
      counts.push(Math.floor(Math.random() * 20) + 1) // 临时模拟数据
    }
    articleTrend.value = { dates, counts }
  } catch (e) {
    console.error('Failed to fetch stats', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="cursor-pointer" @click="router.push('/admin/articles')">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-blue-100 rounded-lg">
              <el-icon size="32" class="text-blue-500"><Document /></el-icon>
            </div>
            <div>
              <p class="text-gray-500 text-sm">文章总数</p>
              <p class="text-2xl font-bold">{{ stats.articleCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="cursor-pointer" @click="router.push('/admin/users')">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-green-100 rounded-lg">
              <el-icon size="32" class="text-green-500"><User /></el-icon>
            </div>
            <div>
              <p class="text-gray-500 text-sm">用户总数</p>
              <p class="text-2xl font-bold">{{ stats.userCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="cursor-pointer" @click="router.push('/admin/comments')">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-orange-100 rounded-lg">
              <el-icon size="32" class="text-orange-500"><ChatDotSquare /></el-icon>
            </div>
            <div>
              <p class="text-gray-500 text-sm">评论总数</p>
              <p class="text-2xl font-bold">{{ stats.commentCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="cursor-pointer" @click="router.push('/admin/categories')">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-purple-100 rounded-lg">
              <el-icon size="32" class="text-purple-500"><FolderOpened /></el-icon>
            </div>
            <div>
              <p class="text-gray-500 text-sm">分类总数</p>
              <p class="text-2xl font-bold">{{ stats.categoryCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <DashboardCharts
      :article-trend="articleTrend"
      :category-distribution="categoryDistribution"
      :tag-usage="tagUsage"
    />
  </div>
</template>