<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getArticleList } from '@/api'
import { getCategoryList } from '@/api'
import { getTagList } from '@/api'
import { getCommentList } from '@/api'
import { getUserList } from '@/api'

const stats = ref({
  articleCount: 0,
  userCount: 0,
  commentCount: 0,
  categoryCount: 0,
  tagCount: 0,
})
const loading = ref(true)

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

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
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
        <el-card shadow="hover">
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
        <el-card shadow="hover">
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
        <el-card shadow="hover">
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
  </div>
</template>