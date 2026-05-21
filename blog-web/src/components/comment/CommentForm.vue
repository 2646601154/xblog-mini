<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { postComment } from '@/api/modules/comment'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  articleId: number
}>()

const emit = defineEmits<{
  success: []
}>()

const authStore = useAuthStore()
const loading = ref(false)
const content = ref('')

async function handleSubmit() {
  if (!content.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (content.value.length > 1000) {
    ElMessage.warning('评论内容最多1000字符')
    return
  }

  loading.value = true
  try {
    await postComment({
      articleId: props.articleId,
      content: content.value.trim(),
    })
    ElMessage.success('评论成功，审核通过后可见')
    content.value = ''
    emit('success')
  } catch {
    ElMessage.error('评论失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="comment-form">
    <h3 class="form-title">发表评论</h3>

    <div class="form-content">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        :disabled="!authStore.isLoggedIn"
        :placeholder="authStore.isLoggedIn ? '写下你的评论... (最多1000字符)' : '登录后即可发表评论'"
        maxlength="1000"
        :show-word-limit="authStore.isLoggedIn"
      />

      <!-- Logged in: show submit button -->
      <div v-if="authStore.isLoggedIn" class="form-actions">
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          提交评论
        </el-button>
      </div>

      <!-- Not logged in: show login link -->
      <div v-else class="login-hint">
        <router-link to="/login" class="login-link">
          登录或注册
        </router-link>
        <span class="hint-text">后即可发表评论</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-form {
  margin-top: var(--space-3xl);
  padding-top: var(--space-3xl);
  border-top: 1px solid var(--border-light);
}

.form-title {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xl);
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.login-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: var(--space-sm) 0;
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.login-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

.hint-text {
  color: var(--text-secondary);
}
</style>
