import { createRouter, createWebHistory } from 'vue-router'

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
      path: '/register',
      name: 'register',
      meta: { layout: 'blank' },
      component: () => import('@/views/register/index.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/index.vue'),
    },
  ],
})

export default router
