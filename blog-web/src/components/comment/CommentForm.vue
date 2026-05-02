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

    <div v-if="!authStore.isLoggedIn" class="login-prompt">
      <span>登录后即可评论</span>
      <router-link to="/login" class="login-link">去登录</router-link>
    </div>

    <div v-else class="form-content">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        placeholder="写下你的评论... (最多1000字符)"
        maxlength="1000"
        show-word-limit
      />
      <div class="form-actions">
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          提交评论
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-form {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid var(--border);
}

.form-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.login-prompt {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius);
  color: var(--text-secondary);
}

.login-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
