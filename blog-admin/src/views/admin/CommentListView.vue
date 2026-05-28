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
    case 'approved':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'approved':
      return '已通过'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已驳回'
    default:
      return status
  }
}

onMounted(fetchComments)
</script>

<template>
  <div class="comment-view">
    <div class="header-section">
      <div class="title-area">
        <h2 class="text-2xl font-bold">评论管理</h2>
        <p class="text-gray-500 text-sm mt-1">管理用户评论，审核评论内容</p>
      </div>
      <el-select
        v-model="filters.status"
        placeholder="状态筛选"
        clearable
        @change="handleFilter"
        style="width: 140px"
      >
        <el-option label="全部" value="" />
        <el-option label="待审核" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
    </div>

    <el-card v-loading="loading" class="comment-card">
      <template #header>
        <div class="card-header">
          <span>评论列表</span>
          <span class="text-gray-400 text-sm">共 {{ total }} 条评论</span>
        </div>
      </template>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-main">
            <div class="comment-header">
              <div class="user-info">
                <el-avatar :size="36" class="user-avatar">
                  {{ comment.user?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-details">
                  <span class="nickname">{{ comment.user?.nickname }}</span>
                  <span class="username">@{{ comment.user?.username }}</span>
                </div>
              </div>
              <div class="comment-meta">
                <el-tag :type="getStatusType(comment.status)" size="small">
                  {{ getStatusText(comment.status) }}
                </el-tag>
                <span class="comment-time">{{ new Date(comment.createdAt).toLocaleString() }}</span>
              </div>
            </div>

            <div class="comment-body">
              <p class="comment-content">{{ comment.content }}</p>
              <div class="article-ref">
                <el-icon><Document /></el-icon>
                <span class="text-gray-500 text-sm">评论文章：</span>
                <span class="article-title">{{ comment.article?.title }}</span>
              </div>
            </div>
          </div>

          <div class="comment-actions" v-if="comment.status === 'pending'">
            <el-button type="success" size="small" @click="handleApprove(comment)">
              <el-icon class="mr-1"><Check /></el-icon>
              通过
            </el-button>
            <el-button type="warning" size="small" @click="handleReject(comment)">
              <el-icon class="mr-1"><Close /></el-icon>
              驳回
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(comment)"
              >删除</el-button
            >
          </div>
          <div class="comment-actions" v-else>
            <el-button type="danger" size="small" link @click="handleDelete(comment)"
              >删除</el-button
            >
          </div>
        </div>
      </div>

      <el-empty v-if="comments.length === 0 && !loading" description="暂无评论" />

      <div class="pagination-wrapper" v-if="total > 0">
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

<style scoped>
.comment-view {
  padding: 24px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.title-area {
  display: flex;
  flex-direction: column;
}

.comment-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 20px;
  border: 1px solid #f0f2f5;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.comment-item:hover {
  border-color: #e8e8e8;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.comment-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #64b3f4);
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 600;
  color: #303133;
}

.username {
  color: #909399;
  font-size: 12px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-body {
  padding-left: 48px;
}

.comment-content {
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
}

.article-ref {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.article-title {
  color: #409eff;
  font-weight: 500;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
  margin-top: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
