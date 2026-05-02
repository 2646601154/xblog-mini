import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/pages/home/index.vue'),
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: () => import('@/pages/article/detail.vue'),
    },
    {
      path: '/category/:slug',
      name: 'category',
      component: () => import('@/pages/category/index.vue'),
    },
    {
      path: '/tag/:slug',
      name: 'tag',
      component: () => import('@/pages/tag/index.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/login/index.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/pages/register/index.vue'),
    },
  ],
})

export default router
