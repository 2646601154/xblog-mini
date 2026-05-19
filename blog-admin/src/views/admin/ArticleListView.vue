<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, publishArticle, recycleArticle, restoreArticle, deleteArticle } from '@/api'
import type { ArticleListItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const articles = ref<ArticleListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = ref({
  status: '',
  categoryId: '',
  title: '',
})

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticleList({
      page: page.value,
      size: size.value,
      status: filters.value.status || undefined,
      categoryId: filters.value.categoryId ? Number(filters.value.categoryId) : undefined,
      title: filters.value.title || undefined,
    })
    articles.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  page.value = 1
  fetchArticles()
}

function handlePageChange(p: number) {
  page.value = p
  fetchArticles()
}

function handleSizeChange(s: number) {
  size.value = s
  fetchArticles()
}

function handleCreate() {
  router.push('/admin/articles/create')
}

function handleEdit(article: ArticleListItem) {
  router.push(`/admin/articles/${article.id}/edit`)
}

async function handlePublish(article: ArticleListItem) {
  try {
    await publishArticle(article.id)
    ElMessage.success('发布成功')
    fetchArticles()
  } catch (e) {
    console.error(e)
  }
}

async function handleRecycle(article: ArticleListItem) {
  try {
    await recycleArticle(article.id)
    ElMessage.success('已移入回收站')
    fetchArticles()
  } catch (e) {
    console.error(e)
  }
}

async function handleRestore(article: ArticleListItem) {
  try {
    await restoreArticle(article.id)
    ElMessage.success('恢复成功')
    fetchArticles()
  } catch (e) {
    console.error(e)
  }
}

async function handleDelete(article: ArticleListItem) {
  try {
    await ElMessageBox.confirm(`确定彻底删除文章「${article.title}」吗？`, '警告', { type: 'warning' })
    await deleteArticle(article.id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

function getStatusType(status: string) {
  switch (status) {
    case 'published': return 'success'
    case 'draft': return 'warning'
    case 'recycled': return 'info'
    default: return 'info'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'published': return '已发布'
    case 'draft': return '草稿'
    case 'recycled': return '已删除'
    default: return status
  }
}

onMounted(fetchArticles)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">文章管理</h2>
      <el-button type="primary" @click="handleCreate">创建文章</el-button>
    </div>

    <el-card>
      <div class="flex gap-4 mb-4">
        <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="已发布" value="published" />
          <el-option label="草稿" value="draft" />
          <el-option label="回收站" value="recycled" />
        </el-select>
        <el-input v-model="filters.title" placeholder="搜索标题" clearable @change="handleFilter" style="width: 200px" />
      </div>

      <el-table :data="articles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <p class="font-medium truncate">{{ row.title }}</p>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category?.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags?.slice(0, 2)" :key="tag.id" size="small" class="mr-1">
              {{ tag.name }}
            </el-tag>
            <span v-if="row.tags?.length > 2" class="text-gray-400 text-xs">+{{ row.tags.length - 2 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" width="100">
          <template #default="{ row }">
            {{ row.viewCount }}
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            {{ row.author?.nickname }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'draft'" type="success" size="small" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 'published'" type="warning" size="small" link @click="handleRecycle(row)">删除</el-button>
            <el-button v-if="row.status === 'draft'" type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            <el-button v-if="row.status === 'recycled'" type="success" size="small" link @click="handleRestore(row)">恢复</el-button>
            <el-button v-if="row.status === 'recycled'" type="danger" size="small" link @click="handleDelete(row)">彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>