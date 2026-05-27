<!-- Generated: 2026-05-27 | Files scanned: 83 | Token estimate: ~700 -->

# Frontend

## blog-admin (Vue 3 + Element Plus)

### Routes

```
/login                   → LoginView.vue
/admin                   → AdminLayout.vue
  /admin                 → DashboardView.vue
  /admin/users           → UserListView.vue
  /admin/articles        → ArticleListView.vue
  /admin/articles/create → ArticleEditView.vue
  /admin/articles/:id/edit → ArticleEditView.vue
  /admin/categories      → CategoryListView.vue
  /admin/tags            → TagListView.vue
  /admin/comments        → CommentListView.vue
  /admin/config          → ConfigView.vue
```

### Stack
- Vue 3 + TypeScript, Vite
- Element Plus (UI), Pinia (state), Vue Router
- Axios → `/v1/admin/*` API

### Key Pages
- ArticleEditView — Markdown 编辑, 标签绑定, 发布/草稿
- DashboardView — 统计概览
- ConfigView — ICP/版权配置

## blog-web (Vue 3 公开前端)

### Stack
- Vue 3, Element Plus (少量), Pinia, Axios
- 公开 API: `/v1/articles`, `/v1/categories`, `/v1/tags`
- 用户: 登录/注册/评论

### Key Features
- 文章列表 + 分页 + 分类/标签筛选
- 文章详情 (Markdown 渲染)
- 评论系统

## blog-mobile (WeChat 小程序)

### Stack
- 原生 WeChat Mini Program (JS)
- 使用相同 API (`/v1/*`)
- Jest 测试

### Pages
- `pages/index` — 首页/文章列表
- `pages/article` — 文章详情
- `pages/user` — 用户中心
- `pages/login` — 登录
