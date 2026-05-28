import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    redirect: '/admin',
  },
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/admin/UserListView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'articles',
        name: 'Articles',
        component: () => import('@/views/admin/ArticleListView.vue'),
        meta: { title: '文章管理' },
      },
      {
        path: 'articles/create',
        name: 'ArticleCreate',
        component: () => import('@/views/admin/ArticleEditView.vue'),
        meta: { title: '创建文章' },
      },
      {
        path: 'articles/:id/edit',
        name: 'ArticleEdit',
        component: () => import('@/views/admin/ArticleEditView.vue'),
        meta: { title: '编辑文章' },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/admin/CategoryListView.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/admin/TagListView.vue'),
        meta: { title: '标签管理' },
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('@/views/admin/CommentListView.vue'),
        meta: { title: '评论管理' },
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/admin/ConfigView.vue'),
        meta: { title: '系统配置' },
      },
      {
        path: 'media',
        name: 'Media',
        component: () => import('@/views/admin/MediaView.vue'),
        meta: { title: '图片管理' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta?.requiresAuth) {
    if (!authStore.isLoggedIn) {
      next('/login')
      return
    }
    if (!authStore.isAdmin) {
      authStore.logout()
      next('/login')
      return
    }
  }

  if (to.path === '/login' && authStore.isLoggedIn && authStore.isAdmin) {
    next('/admin')
    return
  }

  next()
})

export default router
