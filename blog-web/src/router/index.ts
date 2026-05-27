import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/index.vue'),
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: () => import('@/views/article/detail.vue'),
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/categories/index.vue'),
    },
    {
      path: '/categories/:slug',
      name: 'category',
      component: () => import('@/views/categories/index.vue'),
    },
    {
      path: '/login',
      name: 'login',
      meta: { layout: 'blank' },
      component: () => import('@/views/login/index.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/about/index.vue'),
    },
    {
      path: '/archive',
      name: 'archive',
      component: () => import('@/views/archive/index.vue'),
    },
    {
      path: '/register',
      name: 'register',
      meta: { layout: 'blank' },
      component: () => import('@/views/register/index.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      meta: { requiresAuth: true },
      component: () => import('@/views/profile/index.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  if (to.meta?.requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      next('/login')
      return
    }
  }
  next()
})

export default router
