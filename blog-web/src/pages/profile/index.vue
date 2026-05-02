<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<template>
  <div class="profile-page">
    <div class="profile-header">
      <h1 class="page-title">个人中心</h1>
    </div>

    <div class="profile-content">
      <el-card class="profile-card">
        <div class="user-info">
          <el-avatar :size="80" :src="authStore.avatar">
            {{ authStore.nickname?.[0] || 'U' }}
          </el-avatar>
          <div class="user-detail">
            <h2 class="user-name">{{ authStore.nickname || authStore.userInfo?.username }}</h2>
            <p class="user-role">{{ authStore.isAdmin ? '管理员' : '普通用户' }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="profile-card">
        <h3 class="card-title">账号信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ authStore.userInfo?.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">昵称</span>
            <span class="info-value">{{ authStore.userInfo?.nickname }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ authStore.userInfo?.email || '未设置' }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
}

.profile-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border-radius: var(--radius);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--text-primary);
}

.user-role {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.info-value {
  color: var(--text-primary);
  font-size: 0.95rem;
}

@media (max-width: 480px) {
  .user-info {
    flex-direction: column;
    text-align: center;
  }
}
</style>
