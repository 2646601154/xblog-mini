<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCommentList, approveComment, rejectComment, deleteComment } from '@/api'
import type { Comment } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const comments = ref<Comment[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = ref({
  status: '',
})

async function fetchComments() {
  loading.value = true
  try {
    const res = await getCommentList({
      page: page.value,
      size: size.value,
      status: filters.value.status || undefined,
    })
    comments.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  page.value = 1
  fetchComments()
}

function handlePageChange(p: number) {
  page.value = p
  fetchComments()
}

function handleSizeChange(s: number) {
  size.value = s
  fetchComments()
}

async function handleApprove(comment: Comment) {
  try {
    await approveComment(comment.id)
    ElMessage.success('审核通过')
    fetchComments()
  } catch (e) {
    console.error(e)
  }
}

async function handleReject(comment: Comment) {
  try {
    await rejectComment(comment.id)
    ElMessage.success('已驳回')
    fetchComments()
  } catch (e) {
    console.error(e)
  }
}

async function handleDelete(comment: Comment) {
  try {
    await ElMessageBox.confirm(`确定删除该评论吗？`, '警告', { type: 'warning' })
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

function getStatusType(status: string) {
  switch (status) {
    case 'approved': return 'success'
    case 'pending': return 'warning'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'approved': return '已通过'
    case 'pending': return '待审核'
    case 'rejected': return '已驳回'
    default: return status
  }
}

onMounted(fetchComments)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">评论管理</h2>
    </div>

    <el-card>
      <div class="flex gap-4 mb-4">
        <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </div>

      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="文章" min-width="200">
          <template #default="{ row }">
            <p class="truncate">{{ row.article?.title }}</p>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="150">
          <template #default="{ row }">
            <p>{{ row.user?.nickname }}</p>
            <p class="text-gray-400 text-xs">@{{ row.user?.username }}</p>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="250">
          <template #default="{ row }">
            <p class="truncate">{{ row.content }}</p>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="success" size="small" link @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" size="small" link @click="handleReject(row)">驳回</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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