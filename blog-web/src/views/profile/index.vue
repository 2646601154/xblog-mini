<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, changePassword, getMyComments, type UpdateProfileDTO, type ChangePasswordDTO, type CommentMyVO } from '@/api'
import { showSuccessMessage } from '@/utils/error'
import { ElMessage } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'

const authStore = useAuthStore()
const router = useRouter()

// Tab
const activeTab = ref('profile')

// ========== Tab 1 - 编辑资料 ==========
const profileForm = ref<UpdateProfileDTO>({
  nickname: authStore.userInfo?.nickname ?? '',
  email: authStore.userInfo?.email ?? '',
})
const profileLoading = ref(false)

async function handleUpdateProfile() {
  profileLoading.value = true
  try {
    await updateProfile(profileForm.value)
    showSuccessMessage('资料更新成功')
    await authStore.fetchCurrentUser()
  } catch {
    ElMessage.error('更新失败，请重试')
  } finally {
    profileLoading.value = false
  }
}

// ========== Tab 2 - 修改密码 ==========
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordLoading = ref(false)

function passwordError(): string {
  if (!passwordForm.value.oldPassword) return '请输入原密码'
  if (!passwordForm.value.newPassword) return '请输入新密码'
  if (passwordForm.value.newPassword && passwordForm.value.newPassword.length < 6) return '新密码不能少于6位'
  if (passwordForm.value.confirmPassword && passwordForm.value.newPassword !== passwordForm.value.confirmPassword) return '两次输入的密码不一致'
  return ''
}

async function handleChangePassword() {
  const err = passwordError()
  if (err) {
    ElMessage.error(err)
    return
  }
  passwordLoading.value = true
  try {
    const data: ChangePasswordDTO = {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    }
    await changePassword(data)
    showSuccessMessage('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    ElMessage.error('密码修改失败，请检查原密码')
  } finally {
    passwordLoading.value = false
  }
}

// ========== Tab 3 - 我的评论 ==========
const comments = ref<CommentMyVO[]>([])
const commentLoading = ref(false)
const pagination = ref({ current: 1, size: 10, total: 0 })

async function fetchMyComments(page = 1) {
  commentLoading.value = true
  try {
    const res = await getMyComments({ page, size: pagination.value.size })
    comments.value = res.data.data.records
    pagination.value.total = res.data.data.total
    pagination.value.current = res.data.data.page
  } catch {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

function onPageChange(page: number) {
  fetchMyComments(page)
}

function getStatusType(status: string): 'warning' | 'success' | 'danger' {
  const map: Record<string, 'warning' | 'success' | 'danger'> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  }
  return map[status] ?? 'warning'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
  }
  return map[status] ?? status
}

// ========== 退出登录 ==========
function handleLogout() {
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  fetchMyComments()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-card">
      <!-- Avatar Section -->
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="authStore.avatar" class="avatar">
            {{ authStore.nickname?.[0] || 'U' }}
          </el-avatar>
          <div class="avatar-overlay">
            <span class="avatar-icon">✎</span>
          </div>
        </div>
        <p class="avatar-hint">点击更换头像</p>
      </div>

      <!-- Title -->
      <h1 class="profile-title">个人中心</h1>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- Tab 1: 编辑资料 -->
        <el-tab-pane label="编辑资料" name="profile">
          <div class="tab-content">
            <el-form @submit.prevent="handleUpdateProfile">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <el-input :model-value="authStore.userInfo?.username" disabled class="pill-input" />
                <p class="form-help">用户名不可修改</p>
              </div>
              <div class="form-group">
                <label class="form-label">昵称</label>
                <el-input v-model="profileForm.nickname" placeholder="输入昵称" maxlength="50" class="pill-input" />
              </div>
              <div class="form-group">
                <label class="form-label">邮箱</label>
                <el-input v-model="profileForm.email" placeholder="输入邮箱" maxlength="100" class="pill-input" />
              </div>
              <el-button type="primary" :loading="profileLoading" native-type="submit" class="save-btn">
                保存修改
              </el-button>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <el-form @submit.prevent="handleChangePassword">
              <div class="form-group">
                <label class="form-label">原密码</label>
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="输入原密码"
                  class="pill-input"
                />
                <p class="form-help">忘记原密码，请联系管理员</p>
              </div>
              <div class="form-group">
                <label class="form-label">新密码</label>
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="最少6位"
                  class="pill-input"
                />
              </div>
              <div class="form-group">
                <label class="form-label">确认密码</label>
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="再次输入新密码"
                  class="pill-input"
                />
              </div>
              <el-button type="primary" :loading="passwordLoading" native-type="submit" class="save-btn">
                修改密码
              </el-button>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 我的评论 -->
        <el-tab-pane label="我的评论" name="comments">
          <div class="tab-content">
            <div v-if="commentLoading" class="loading-state">
              <el-icon class="is-loading" :size="24"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <el-empty v-else-if="!comments.length" description="暂无评论" />
            <div v-else class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <router-link :to="`/article/${comment.article.id}`" class="comment-article">
                    {{ comment.article.title }}
                  </router-link>
                  <el-tag :type="getStatusType(comment.status)" size="small" effect="plain">
                    {{ getStatusLabel(comment.status) }}
                  </el-tag>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
                <p class="comment-meta">{{ comment.createdAt }}</p>
              </div>
              <div class="pagination-wrapper" v-if="pagination.total > pagination.size">
                <AppPagination
                  v-model:current="pagination.current"
                  :total="pagination.total"
                  :page-size="pagination.size"
                  @change="onPageChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- Logout -->
      <div class="logout-section">
        <el-button type="danger" plain @click="handleLogout" class="logout-btn">
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-3xl) var(--space-lg);
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.profile-card {
  width: 100%;
  max-width: 560px;
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-3xl);
}

/* Avatar Section */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar {
  border: 3px solid var(--color-primary-100);
  border-radius: 50%;
  transition: border-color 0.2s ease;
}

.avatar-wrapper:hover .avatar {
  border-color: var(--color-primary);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(139, 92, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-icon {
  font-size: var(--text-lg);
  color: var(--color-primary);
}

.avatar-hint {
  margin: var(--space-sm) 0 0 0;
  font-size: var(--text-xs);
  color: var(--text-muted);
}

/* Title */
.profile-title {
  text-align: center;
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2xl) 0;
}

/* Tabs */
.profile-tabs {
  margin-bottom: var(--space-xl);
}

:deep(.el-tabs__item) {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-light);
}

/* Tab Content */
.tab-content {
  padding: var(--space-lg) 0;
}

/* Form */
.form-group {
  margin-bottom: var(--space-lg);
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.pill-input {
  --el-input-border-radius: var(--radius-pill);
  --el-input-height: 44px;
}

:deep(.pill-input .el-input__wrapper) {
  border-radius: var(--radius-pill);
  box-shadow: none;
  border: 1px solid var(--border-light);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.pill-input .el-input__wrapper:hover),
:deep(.pill-input .el-input__wrapper:focus-within) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-50);
}

:deep(.pill-input .el-input__wrapper.is-disabled) {
  background-color: var(--bg-elevated);
  border-color: var(--border-light);
}

.form-help {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin: var(--space-sm) 0 0 0;
}

/* Save Button */
.save-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-pill);
  font-weight: 600;
  font-size: var(--text-base);
  margin-top: var(--space-lg);
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.save-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  opacity: 0.9;
}

/* Loading State */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: var(--space-3xl) 0;
  color: var(--text-secondary);
}

/* Comment List */
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  padding: var(--space-lg) 0;
  border-bottom: 1px solid var(--border-light);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  margin-bottom: var(--space-sm);
}

.comment-article {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-primary);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-article:hover {
  text-decoration: underline;
}

.comment-content {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-word;
}

.comment-meta {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-xl);
}

/* Logout Section */
.logout-section {
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-xl);
  text-align: center;
}

.logout-btn {
  border-radius: var(--radius-pill);
  padding: var(--space-md) var(--space-2xl);
}

@media (max-width: 480px) {
  .profile-page {
    padding: var(--space-lg);
  }

  .profile-card {
    padding: var(--space-2xl);
  }

  .avatar-section {
    margin-bottom: var(--space-lg);
  }
}
</style>