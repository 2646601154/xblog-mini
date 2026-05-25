# blog-admin 代码上下文

## 项目概览

轻量级博客管理后台（Vue 3 + TypeScript + Vite + Tailwind CSS v4），与 blog-api (Java/Spring Boot) 前后端分离部署。

## 目录结构

```
blog-admin/src/
├── api/
│   ├── request.ts       # Axios实例 + 双Token刷新机制 + 拦截器
│   ├── index.ts         # 统一导出
│   └── modules/         # auth, user, article, category, tag, comment, config
├── components/
│   ├── layout/          # AdminLayout, AdminSidebar, AdminHeader
│   ├── charts/          # DashboardCharts (ECharts)
│   └── rich-editor/     # WangEditor 富文本编辑器
├── router/index.ts      # 路由 + beforeEach 守卫
├── stores/auth.ts       # Pinia auth store (Composition API)
├── types/index.ts       # TypeScript 类型定义
├── utils/storage.ts     # localStorage 封装
├── views/
│   ├── login/LoginView.vue
│   └── admin/           # Dashboard, ArticleList, ArticleEdit, Category, Tag, Comment, User, Config
└── styles/main.css      # Tailwind CSS 入口
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Vue 3.5 + Composition API + `<script setup>` |
| 构建 | Vite 8 + TypeScript 6 |
| 路由 | Vue Router 5 |
| 状态 | Pinia 3 |
| UI | Element Plus 2.13 + Tailwind CSS v4 (`@tailwindcss/vite`) |
| HTTP | Axios 1.16 + 双Token刷新 |
| 图表 | ECharts 6 + vue-echarts |
| 编辑器 | WangEditor 5 |

## 关键实现

### 双Token机制 (request.ts)
- `accessToken` 短期有效，存内存+localStorage
- `refreshToken` 长期有效，仅存localStorage
- 401时自动刷新，多请求排队等待 (`refreshSubscribers` 数组)
- 刷新失败弹窗让用户选择保持页面或跳转登录

### 认证流程 (stores/auth.ts)
```ts
initAuth()      // 应用启动时从localStorage恢复会话
login()         // 登录 → 存Token → fetchCurrentUser()
logout()        // 调用API登出 → clearAuth()
refreshAccessToken()  // 主动刷新
```

### 路由守卫 (router/index.ts)
- `/admin/*` 需要登录 + admin角色
- 未登录重定向 `/login`
- 已登录访问 `/login` 重定向 `/admin`

### API模块化
- `src/api/request.ts` 统一Axios实例，baseURL=`/dev-api`（开发）
- `src/api/modules/*.ts` 分离各模块API，类型定义在 `src/types/index.ts`

## 数据流

```
用户操作 → Vue组件 → Pinia Store → API模块 → Axios请求 → 后端
                    ↓
              localStorage (持久化Token/UserInfo)
```

## 入口文件

| 文件 | 作用 |
|------|------|
| `src/main.ts` | 创建App，挂载Pinia/Router/ElementPlus |
| `src/App.vue` | 根组件，`<router-view />` |
| `vite.config.ts` | 开发代理 `/dev-api` → `localhost:8080` |

## 注意事项

- localStorage key前缀：`admin_*`（与 blog-web 的 `xblog_*` 区分）
- API基础路径：Vite代理 `/dev-api` → 后端 `http://localhost:8080`
- Token刷新队列使用数组而非Promise链，需注意并发场景
- DashboardView 使用 `Math.random()` 模拟分类/标签文章数（临时方案）