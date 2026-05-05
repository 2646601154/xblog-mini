# blog-admin 管理后台开发指南

**Generated:** 2026-05-05
**父级**: `../AGENTS.md`

## OVERVIEW

管理后台（Vue 3 + TypeScript + Vite + Pinia + Tailwind CSS），面向管理员。

## STRUCTURE

```
blog-admin/src/
├── api/              # Axios封装 + 模块化endpoint
│   ├── request.ts    # Axios实例、拦截器（与blog-web一致）
│   └── modules/      # auth.ts, user.ts, article.ts, category.ts, tag.ts, comment.ts, config.ts
├── components/
│   ├── layout/       # AdminLayout.vue, AdminSidebar.vue, AdminHeader.vue
│   └── common/
├── router/          # Vue Router（路由守卫 + 懒加载）
├── stores/          # Pinia stores（auth.ts）
├── types/          # TypeScript类型定义
├── utils/          # 工具函数
├── views/           # 页面视图
│   ├── admin/       # Dashboard, Users, Articles, Categories, Tags, Comments, Config
│   └── login/       # LoginView.vue
└── styles/         # Tailwind CSS 全局样式
```

## WHERE TO LOOK

| 任务 | 路径 |
|------|------|
| API调用 | `src/api/request.ts` + `src/api/modules/` |
| 认证状态 | `src/stores/auth.ts` |
| 路由守卫 | `src/router/index.ts`（`beforeEach` 检查登录态） |
| 仪表盘数据 | `src/views/admin/DashboardView.vue` |
| 侧边栏导航 | `src/components/layout/AdminSidebar.vue` |

## CONVENTIONS

### API层
- 与 blog-web 相同模式：`src/api/request.ts`
- 基础路径：`/api/v1/`
- 管理端接口：`/api/v1/admin/...`

### Pinia Store
- 与 blog-web 一致：`defineStore('auth', () => { ... })`
- `login()` → `ElMessage.success` 提示
- `initAuth()` 在 `main.ts` 调用，恢复会话

### 路由
- 受保护路由：`/admin/*`，`beforeEach` 守卫检查 `useAuthStore().isLoggedIn`
- 未登录 → 重定向 `/login`
- 子路由：`/admin/dashboard`, `/admin/users`, `/admin/articles`, `/admin/categories`, `/admin/tags`, `/admin/comments`, `/admin/config`

### UI
- Tailwind CSS + PostCSS（与 blog-web 的 Element Plus 不同）
- AdminLayout：左侧边栏 + 顶部Header + main内容区
- 使用 `ElMessage` / `ElMessageBox` 等 Element Plus 组件做提示

## ANTI-PATTERNS

- ❌ **不要**绕过路由守卫直接访问管理页面
- ❌ **不要**在组件内直接 `localStorage`，用 `src/utils/storage.ts`
- ❌ **不要**硬编码API路径

## COMMANDS

```bash
cd blog-admin
pnpm install
pnpm dev     # 开发服务器
pnpm build   # 生产构建
pnpm preview # 预览
```
