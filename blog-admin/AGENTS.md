# blog-admin 管理后台开发指南

**Generated:** 2026-07-08
**父级**: `../AGENTS.md`

## OVERVIEW

管理后台（Vue 3 + TypeScript + Vite + Pinia + Tailwind CSS v4 + Element Plus），面向管理员。

## STRUCTURE

```
blog-admin/src/
├── api/                  # Axios封装 + 模块化endpoint
│   ├── request.ts        # Axios实例、拦截器、401自动刷新队列
│   ├── index.ts          # barrel 导出全部模块
│   └── modules/          # auth, user, article, category, tag, comment, config, dashboard, upload, media
├── components/
│   ├── layout/           # AdminLayout.vue, AdminSidebar.vue, AdminHeader.vue
│   ├── charts/           # DashboardCharts.vue（echarts + vue-echarts）
│   └── rich-editor/      # RichEditor.vue（@wangeditor/editor）
├── router/               # Vue Router（路由守卫 + 懒加载）
├── stores/               # Pinia stores（auth.ts）
├── types/                # TypeScript类型定义（index.ts）
├── utils/                # 工具函数（storage.ts）
├── views/
│   ├── admin/            # Dashboard, Users, Articles, Categories, Tags, Comments, Config, Media
│   └── login/            # LoginView.vue
└── styles/               # Tailwind CSS v4 全局样式（@import 'tailwindcss'）
```

## WHERE TO LOOK

| 任务 | 路径 |
|------|------|
| API调用 | `src/api/request.ts` + `src/api/modules/` |
| 认证状态 | `src/stores/auth.ts` |
| 路由守卫 | `src/router/index.ts`（`beforeEach` 检查 `isLoggedIn` + `isAdmin`） |
| 仪表盘数据 | `src/views/admin/DashboardView.vue` + `src/components/charts/DashboardCharts.vue` |
| 侧边栏导航 | `src/components/layout/AdminSidebar.vue` |
| 富文本编辑 | `src/components/rich-editor/RichEditor.vue`（wangEditor 5） |

## CONVENTIONS

### API层
- 请求实例：`src/api/request.ts`，Axios 封装
- `baseURL`：`import.meta.env.VITE_API_BASE_URL`（开发环境 `/dev-api` → Vite proxy → `localhost:8080`；生产环境 `/api`）
- 管理端接口：`/v1/admin/...`，认证接口：`/v1/auth/...`
- 响应格式：`{ code, message, data }`，`code === 200` 表示成功
- `code === 1001 | 1002` = session 过期（触发弹窗 + 刷新或登出）
- **Token 刷新机制**（`request.ts`）：
  - 401 时自动尝试 `POST /v1/auth/refresh`
  - 并发 401 仅触发一次刷新，其余请求排队等待（`refreshSubscribers` 队列）
  - 刷新失败 → `ElMessageBox.confirm` 弹窗（"跳转至登录 / 保持此页"），去重保护
  - 已在 `/login` 页面时不弹窗
- `src/stores/auth.ts` 另有 `refreshAccessToken()`，两套刷新路径并存

### Pinia Store
- `defineStore('auth', () => { ... })` Composition API 风格
- `login()` → 存储 token + 调 `fetchCurrentUser()`
- `initAuth()` 在 `main.ts` 调用，从 localStorage 恢复会话（纯水合，不验证 token 有效性）
- `isLoggedIn` = `!!token`，`isAdmin` = `userInfo.role === 'admin'`

### 路由
- 受保护路由：`/admin/*`，`meta: { requiresAuth: true }`
- `beforeEach` 守卫：未登录 → `/login`；非 admin → `logout()` + `/login`
- 已登录 admin 访问 `/login` → 重定向 `/admin`
- 完整子路由：`/admin/dashboard`, `/admin/users`, `/admin/articles`, `/admin/articles/create`, `/admin/articles/:id/edit`, `/admin/categories`, `/admin/tags`, `/admin/comments`, `/admin/config`, `/admin/media`

### UI
- **Tailwind CSS v4**（`@import 'tailwindcss'` + `@tailwindcss/vite` 插件）用于布局和自定义样式
- **Element Plus** 全局注册（`app.use(ElementPlus)`），所有图标全局注册
- 消息提示使用 `ElMessage` / `ElMessageBox`
- AdminLayout：左侧边栏 + 顶部 Header + `<el-main>` 内容区

### 工具链
- ESLint 10 扁平配置（`eslint.config.ts`），含 `@vue/eslint-config-typescript` + `eslint-plugin-vue` + `eslint-plugin-oxlint` + `eslint-config-prettier`
- Prettier（`.prettierrc.json`）：无分号、单引号、100 字符宽度
- `.editorconfig`：UTF-8、2 空格缩进、LF 换行
- 构建输出目录：`dist-admin`（`vite.config.ts` 中 `outDir: 'dist-admin'`）
- Node 引擎要求：`^20.19.0 || >=22.12.0`

### 响应式数据约定
- localStorage 键前缀：`admin_`（`admin_access_token`, `admin_refresh_token`, `admin_user_info`）
- 通用分页类型：`PageResult<T>`（`records`, `total`, `page`, `size`）
- 通用响应包裹：`ApiResponse<T>`（`code`, `message`, `data`）

## ANTI-PATTERNS

- ❌ **不要**绕过路由守卫直接访问管理页面
- ❌ **不要**在组件内直接 `localStorage`，用 `src/utils/storage.ts`
- ❌ **不要**硬编码API路径
- ❌ **不要**使用公开 `GET /v1/articles/{id}` 获取管理端文章详情（每次请求增加浏览量，BUG-4），使用 `GET /v1/admin/articles/{id}`（支持 draft/recycled，无副作用）

## COMMANDS

```bash
cd blog-admin
pnpm install          # 安装依赖
pnpm dev              # 开发服务器（Vite proxy → localhost:8080）
pnpm build            # 生产构建（type-check + build-only 并行）
pnpm preview          # 预览生产构建
pnpm type-check       # vue-tsc --build（类型检查）
pnpm lint             # eslint src/
pnpm lint:fix         # eslint src/ --fix
pnpm format           # prettier --write src/
```
