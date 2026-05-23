<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Platform, ChatDotRound, Message } from '@element-plus/icons-vue'
import { useConfigStore } from '@/stores/config'
import { getArticleList, getCategoryList, getTagList } from '@/api'

const configStore = useConfigStore()

const stats = ref({
  articles: 0,
  categories: 0,
  tags: 0,
})

const socialLinks = [
  { icon: 'platform', url: 'https://home.xiaruoxin.cn', label: 'GitHub' },
  { icon: 'email', url: 'https://home.xiaruoxin.cn', label: 'Email' },
  { icon: 'bilibili', url: 'https://home.xiaruoxin.cn', label: 'bilibili' },
]

const getIconComponent = (icon: string) => {
  const iconMap: Record<string, unknown> = {
    platform: Platform,
    chat: ChatDotRound,
    email: Message,
    bilibili: ChatDotRound,
  }
  return iconMap[icon] || Message
}

async function loadStats() {
  try {
    const [articleRes, categoryRes, tagRes] = await Promise.all([
      getArticleList({ page: 1, size: 1 }),
      getCategoryList(),
      getTagList(),
    ])
    stats.value = {
      articles: articleRes.data.data.total,
      categories: categoryRes.data.data.length,
      tags: tagRes.data.data.length,
    }
  } catch {
    // Silent fail, use placeholder values
  }
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="about-page">
    <div class="about-container">
      <!-- Hero Section -->
      <section class="hero">
        <img :src="configStore.siteLogo || '/avatar/LuoXiaohei.jpg'" alt="Avatar" class="hero-avatar"
          @error="($event.target as HTMLImageElement).src = '/avatar/LuoXiaohei.jpg'" />
        <h1 class="name">Xiaruoxin</h1>
        <p class="title">热爱技术与分享的全栈开发者</p>
        <div class="social-links">
          <div v-for="link in socialLinks" :key="link.label" class="social-item">
            <a :href="link.url" :title="link.label" class="social-btn" target="_blank" rel="noopener noreferrer">
              <el-icon :size="18">
                <component :is="getIconComponent(link.icon)" />
              </el-icon>
            </a>
            <span class="social-label">{{ link.label }}</span>
          </div>
        </div>
      </section>

      <!-- About Section -->
      <section class="about-section">
        <h2 class="section-title">关于博客</h2>
        <div class="about-content">
          <p>
            这是一个专注于技术分享的个人博客，记录我在全栈开发道路上的探索与成长。
            从前端的 Vue/React 到后端的 Spring Boot，从数据库设计到微服务架构，
            我致力于将所学所见整理成文，帮助更多开发者解决实际问题。
          </p>
          <p>
            博客最初源于对知识的渴望和分享的热情。在学习和实践的过程中，
            我深刻体会到「教是最好的学」——通过写文章，我能够更深入地理解技术原理，
            同时也能与社区中的同行交流切磋，共同进步。
          </p>
          <p>
            如果你在阅读文章时有任何问题或建议，欢迎随时与我交流。
            让 我们一起在技术的海洋中不断探索，共同成长！
          </p>
        </div>
      </section>

      <!-- Stats Section -->
      <section class="stats-section">
        <h2 class="section-title">博客数据</h2>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-number">{{ stats.articles }}</div>
            <div class="stat-label">文章</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.categories }}</div>
            <div class="stat-label">分类</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.tags }}</div>
            <div class="stat-label">标签</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--space-5xl) var(--space-lg);
}

.hero {
  text-align: center;
}

.hero-avatar {
  display: block;
  margin: 0 auto;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid var(--color-primary-100);
  object-fit: cover;
}

.name {
  font-size: var(--text-3xl);
  font-weight: 700;
  margin-top: var(--space-xl);
  color: var(--text-primary);
}

.title {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin-top: var(--space-sm);
}

.social-links {
  display: flex;
  justify-content: center;
  flex-direction: row;
  gap: var(--space-lg);
  margin-top: var(--space-xl);
}

.social-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
}

.social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-pill);
  background: var(--bg-elevated);
  color: var(--text-secondary);
  text-decoration: none;
  transition: background-color 0.2s, color 0.2s;
}

.social-btn:hover {
  background: var(--color-primary-50);
  color: var(--color-primary);
}

.social-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.section-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin: var(--space-4xl) 0 var(--space-xl);
  color: var(--text-primary);
}

.about-content p {
  font-size: var(--text-base);
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: var(--space-lg);
}

.about-content p:last-child {
  margin-bottom: 0;
}

.stats-section {
  margin-top: var(--space-4xl);
}

.stats-cards {
  display: flex;
  gap: var(--space-lg);
}

.stat-card {
  flex: 1;
  background: var(--bg-elevated);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  text-align: center;
}

.stat-number {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-top: var(--space-sm);
}

@media (max-width: 768px) {
  .stats-cards {
    flex-direction: column;
  }
}
</style>
