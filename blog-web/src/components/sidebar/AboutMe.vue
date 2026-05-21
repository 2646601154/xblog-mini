<script setup lang="ts">
import { Platform, ChatDotRound, Message } from '@element-plus/icons-vue'

interface SocialLink {
  icon: string
  url: string
  label: string
}

withDefaults(defineProps<{
  avatar?: string
  bio?: string
  socialLinks?: SocialLink[]
}>(), {
  avatar: 'https://via.placeholder.com/64',
  bio: '热爱技术与分享的全栈开发者',
  socialLinks: () => [
    { icon: 'platform', url: 'https://github.com', label: 'GitHub' },
    { icon: 'chat', url: 'https://twitter.com', label: 'Twitter' },
    { icon: 'email', url: 'mailto:example@example.com', label: 'Email' }
  ]
})

const getIconComponent = (icon: string) => {
  const iconMap: Record<string, unknown> = {
    platform: Platform,
    chat: ChatDotRound,
    email: Message
  }
  return iconMap[icon] || Message
}
</script>

<template>
  <div class="about-me">
    <div class="avatar-wrapper">
      <img :src="avatar" alt="Avatar" class="avatar" />
    </div>
    <h3 class="title">关于博主</h3>
    <p class="bio">{{ bio }}</p>
    <div class="social-links">
      <a
        v-for="link in socialLinks"
        :key="link.label"
        :href="link.url"
        :title="link.label"
        class="social-btn"
        target="_blank"
        rel="noopener noreferrer"
      >
        <el-icon :size="16">
          <component :is="getIconComponent(link.icon)" />
        </el-icon>
      </a>
    </div>
  </div>
</template>

<style scoped>
.about-me {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-md);
  margin-bottom: 20px;
  text-align: center;
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid var(--color-primary-100);
  object-fit: cover;
}

.title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-top: var(--space-md);
  margin-bottom: var(--space-sm);
  color: var(--text-primary);
}

.bio {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 var(--space-lg) 0;
}

.social-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-md);
}

.social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-pill);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s ease;
}

.social-btn:hover {
  background: var(--color-primary-50);
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .about-me {
    padding: var(--space-lg);
  }
}
</style>