# blog-web 前端开发指南

**Generated:** 2026-05-05
**父级**: `../AGENTS.md`

## OVERVIEW

前台展示端（Vue 3 + TypeScript + Vite + Pinia + Element Plus），面向普通访客。

## STRUCTURE

```
blog-web/src/
├── api/              # Axios封装 + 模块化endpoint
│   ├── request.ts    # Axios实例、拦截器（请求/响应）
│   └── modules/      # auth.ts, article.ts, category.ts, tag.ts, comment.ts, config.ts
├── components/       # 复用组件
│   ├── article/      # ArticleCard.vue
│   ├── comment/      # CommentList.vue, CommentForm.vue
│   ├── common/       # AppPagination.vue
│   ├── layout/       # AppLayout.vue, AppHeader.vue, AppFooter.vue
│   └── sidebar/      # CategoryList.vue, TagCloud.vue
├── pages/            # 路由页面（按URL组织）
│   ├── home/         # 首页
│   ├── article/      # 文章详情
│   ├── categories/   # 分类列表
│   ├── tag/          # 标签文章
│   ├── login/
│   ├── register/
│   └── profile/
├── router/           # Vue Router（懒加载组件）
├── stores/           # Pinia stores（auth.ts）
├── types/           # TypeScript类型定义
├── utils/           # 工具函数（storage.ts）
└── styles/          # 全局样式
```

## WHERE TO LOOK

| 任务 | 路径 |
|------|------|
| API调用 | `src/api/request.ts` + `src/api/modules/` |
| 认证状态 | `src/stores/auth.ts` |
| 路由定义 | `src/router/index.ts` |
| 首页逻辑 | `src/pages/home/index.vue` |

## CONVENTIONS

### API层
- Axios实例：`src/api/request.ts`，baseURL=`VITE_API_BASE_URL`，超时10s
- 请求拦截器：自动附加 `Authorization: Bearer {token}`
- 响应拦截器：处理业务错误码（1001/1002跳转登录），统一错误提示
- Token存储：`src/utils/storage.ts`（localStorage封装）

### Pinia Store
- 使用 Composition API 风格：`defineStore('auth', () => { ... })`
- 暴露：`token`, `userInfo`, `isLoggedIn`, `isAdmin` + `login/logout/fetchCurrentUser/initAuth`
- `initAuth()` 在 `main.ts` 的 router ready 后调用，恢复登录态

### 路由
- 使用 `createWebHistory` + 懒加载组件
- `component: () => import('@/pages/...')`
- 公开路由：`/`, `/article/:id`, `/categories`, `/tag/:slug`, `/login`, `/register`, `/profile`

## ANTI-PATTERNS

- ❌ **不要**在组件内直接调用 `localStorage`，用 `src/utils/storage.ts` 封装
- ❌ **不要**硬编码API路径，用 `src/api/modules/` 中已封装的函数
- ❌ **不要**在组件内直接 `axios.get()`，统一走 `src/api/request.ts`

## COMMANDS

```bash
cd blog-web
pnpm install
pnpm dev     # 开发服务器
pnpm build   # 生产构建（先type-check）
pnpm preview # 预览构建产物
```

## NOTES

- `dist-web/` 为构建产物目录，不应提交到仓库
- 存在嵌套的 `blog-web/blog-web/` 目录，疑似构建产物混淆
