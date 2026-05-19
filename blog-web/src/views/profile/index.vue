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
    <div class="profile-header">
      <h1 class="page-title">个人中心</h1>
    </div>

    <!-- 用户信息卡片 -->
    <el-card class="user-info-card" shadow="never">
      <div class="user-info">
        <el-avatar :size="72" :src="authStore.avatar">
          {{ authStore.nickname?.[0] || 'U' }}
        </el-avatar>
        <div class="user-detail">
          <h2 class="user-name">{{ authStore.nickname || authStore.userInfo?.username }}</h2>
          <p class="user-role">{{ authStore.isAdmin ? '管理员' : '普通用户' }}</p>
          <p class="user-joined">
            <template v-if="authStore.userInfo?.email">邮箱：{{ authStore.userInfo.email }}</template>
          </p>
        </div>
      </div>
    </el-card>

    <!-- Tab 切换 -->
    <el-card class="profile-tabs-card" shadow="never">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- Tab 1: 编辑资料 -->
        <el-tab-pane label="编辑资料" name="profile">
          <div class="tab-content">
            <el-form label-width="80px" @submit.prevent="handleUpdateProfile">
              <el-form-item label="用户名">
                <el-input :model-value="authStore.userInfo?.username" disabled />
                <p class="form-help">用户名不可修改</p>
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" placeholder="输入昵称" maxlength="50" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="输入邮箱" maxlength="100" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" native-type="submit">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <el-form label-width="100px" @submit.prevent="handleChangePassword">
              <el-form-item label="原密码">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="输入原密码"
                />
                <p class="form-help">忘记原密码，请联系管理员</p>
              </el-form-item>
              <el-form-item label="新密码">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="最少6位"
                />
              </el-form-item>
              <el-form-item
                label="确认密码"
                :error="passwordForm.confirmPassword ? passwordError() : ''"
              >
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="再次输入新密码"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="passwordLoading" native-type="submit">
                  修改密码
                </el-button>
              </el-form-item>
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

      <!-- 退出登录 -->
      <div class="logout-section">
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 30px 20px;
}

.profile-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.user-info-card {
  margin-bottom: 20px;
  border-radius: var(--radius);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text-primary);
}

.user-role {
  font-size: 0.9rem;
  color: var(--accent);
  margin: 0 0 4px 0;
}

.user-joined {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin: 0;
}

.profile-tabs-card {
  border-radius: var(--radius);
}

.profile-tabs {
  --el-tabs-header-height: 42px;
}

.tab-content {
  padding: 20px 0;
  min-height: 200px;
}

.form-help {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin: 4px 0 0 0;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: var(--text-secondary);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-article {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-article:hover {
  text-decoration: underline;
}

.comment-content {
  margin: 0 0 8px 0;
  font-size: 0.9rem;
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-word;
}

.comment-meta {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.logout-section {
  border-top: 1px solid var(--border);
  padding-top: 20px;
  text-align: center;
}

@media (max-width: 480px) {
  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .profile-page {
    padding: 20px 16px;
  }
}
</style>
