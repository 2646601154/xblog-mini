# Xblog-mini PROJECT KNOWLEDGE BASE

**Generated:** 2026-07-09
**Commit:** 3a10d3c
**Branch:** main

## OVERVIEW

轻量级个人博客系统（MVP）。4模块：Java/Spring Boot后端 + Vue3前台 + Vue3管理后台 + 微信小程序。

## STRUCTURE

```
Xblog-mini/
├── blog-api/          # Java 17 + Spring Boot 3.5.14 后端（121个Java文件，18个包）
├── blog-web/          # Vue 3 + TypeScript + Vite 前台（Element Plus）
├── blog-admin/        # Vue 3 + TypeScript + Vite 管理后台（Tailwind CSS v4 + Element Plus）
├── blog-mobile/       # 微信小程序（原生App.js入口，Jest测试）
├── docker/            # Docker 6服务编排（nginx-proxy/mysql/redis/api/web/admin）
├── doc/api/           # API接口文档
├── sql/               # 数据库初始化脚本
├── .github/workflows/ # GitHub Actions（空目录，待配置）
└── AGENTS.md          # 本文件
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 后端开发规范 | `blog-api/AGENTS.md` | 接口开发流程 |
| 前台API调用 | `blog-web/src/api/modules/` | 7模块：auth/article/category/comment/tag/config/upload |
| 管理端API调用 | `blog-admin/src/api/modules/` | 10模块：比前台多dashboard/media/user |
| 前台状态管理 | `blog-web/src/stores/` | auth.ts / config.ts / theme.ts（暗色模式） |
| 管理端状态管理 | `blog-admin/src/stores/` | auth.ts |
| 前台路由 | `blog-web/src/router/index.ts` | Vue Router + 路由守卫（requiresAuth） |
| 管理端路由 | `blog-admin/src/router/index.ts` | 路由守卫（isLoggedIn + isAdmin） |
| 前台类型定义 | `blog-web/src/types/index.ts` | TypeScript类型 |
| 业务错误码 | `blog-api/.../common/enums/ResultCode.java` | 错误码定义（含脱敏文案） |
| 错误提示映射 | `blog-web/src/utils/error.ts` | 前端错误码→中文提示映射 |
| 测试 | `blog-api/src/test/java/com/xblog/` | 8个测试类（JUnit 5 + Spring Boot Test） |
| 微信小程序 | `blog-mobile/` | 原生小程序，非Vue架构 |
| Docker部署 | `docker/AGENTS.md` | 6服务编排 + SSL自动续期 |

## ARCHITECTURE

### blog-api（后端）
- **入口**: `com.xblog.Main`（非典型命名）
- **Controller层**: 公开（Article/Auth/Category/Comment/Config/Tag/User）+ 管理端（Article/Category/Comment/Config/Dashboard/Media/Tag/Upload/User）
- **Service层**: 8个Service + 8个Impl（Article/Category/Comment/Config/Dashboard/Media/Tag/User）
- **DAO层**: MyBatis-Plus Mapper（7个Mapper）
- **通用组件**: JWT双Token、ThreadLocal UserContext、Redis缓存、OSS文件存储、全局异常处理、Knife4j API文档

### blog-web（前台）
- **UI**: Element Plus（全局注册，含图标）
- **Store**: Pinia Composition API（auth/config/theme）
- **主题**: 暗色模式（data-theme属性 + localStorage持久化 + 系统偏好监听）
- **构建输出**: `dist-web/`

### blog-admin（管理后台）
- **UI**: Tailwind CSS v4（`@import 'tailwindcss'` + `@tailwindcss/vite` 插件）+ Element Plus
- **编辑器**: wangEditor 5（`@wangeditor/editor`）
- **图表**: ECharts 6 + vue-echarts
- **构建输出**: `dist-admin/`
- **工具链**: ESLint 10扁平配置、Prettier（无分号/单引号/100字符）、EditorConfig

### blog-mobile（微信小程序）
- **入口**: `app.js`（原生小程序）
- **服务层**: `services/`（article/auth/category/comment）
- **测试**: Jest + jest-miniprogram preset

## CONVENTIONS

### 双Token机制
- `accessToken`（15分钟） + `refreshToken`（7天）分离存储与刷新
- 401自动尝试刷新，并发401仅触发一次，其余排队（`refreshSubscribers` 队列）
- 刷新失败 → `ElMessageBox.confirm` 弹窗（"跳转至登录 / 保持此页"），去重保护
- 已在 `/login` 页面时不弹窗
- 两套刷新路径并存：`request.ts` 拦截器 + `authStore.refreshAccessToken()`

### localStorage前缀
- blog-web: `xblog_`（`xblog_access_token`, `xblog_refresh_token`, `xblog_user_info`, `xblog_theme`, `xblog_remember`）
- blog-admin: `admin_`（`admin_access_token`, `admin_refresh_token`, `admin_user_info`）

### API约定
- 响应格式：`{ code, message, data }`，`code === 200` 成功
- `code === 1001 | 1002` = 会话过期
- 基础路径：`/v1/`（公开）/ `/v1/admin/`（管理端）
- 开发环境 Vite proxy：`/dev-api` → `localhost:8080`

### 后端约定
- MybatisPlusFillMetaObjectHandler 自动填充 `createdAt`/`updatedAt`
- 逻辑删除：仅 `article` 表使用，其他表物理删除
- 包名拼写错误：`common.intercepter` 应为 `common.interceptor`（历史遗留）
- 空包残留：`com.xblog.xblog` 和 `security/` 目录为空
- 脱敏错误码：跨用户写操作返回"操作被拒绝"，具体原因在服务端日志记录

## COMMANDS

```bash
# 后端启动
cd blog-api
./mvnw spring-boot:run

# 后端测试
cd blog-api
./mvnw test

# 前台启动
cd blog-web
pnpm install
pnpm dev
pnpm build        # type-check + build-only 并行
pnpm lint         # eslint . --fix
pnpm format       # prettier --write src/

# 管理后台启动
cd blog-admin
pnpm install
pnpm dev
pnpm build        # type-check + build-only 并行
pnpm lint         # eslint src/
pnpm lint:fix     # eslint src/ --fix
pnpm format       # prettier --write src/

# 微信小程序测试
cd blog-mobile
npm test
npm run test:coverage

# Docker部署
cd docker
docker-compose up -d --build

# 数据库
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

## ANTI-PATTERNS（THIS PROJECT）

- ❌ **不要**在 `node_modules` 目录放置项目文件（已发现误提交）
- ❌ **不要**忘记分页null兜底：`int pageNum = dto.getPage() != null ? dto.getPage() : 1`
- ❌ **不要**在循环内单独查询关联数据，用 `selectBatchIds` 批量查询
- ❌ **不要**修改配置后忘记清理Redis缓存
- ❌ **不要**忘记清理 ThreadLocal（`UserContext` 使用 ThreadLocal 存储用户信息，需确保在请求结束后调用 `clear()`）
- ❌ **不要**在 `.env` 中使用弱密码或默认密码（Docker部署）
- ❌ **不要**将 `dist-web/`、`dist-admin/`、`*.jar` 提交到Git（构建产物）
- ❌ **不要**使用 `console.error` 记录生产日志（前端12+处违规）
- ❌ **不要**用 `catch (Exception e)` 吞原始异常（OssUtil/ArticleServiceImpl共3处）
- ❌ **不要**使用公开 `GET /v1/articles/{id}` 获取管理端文章详情（每次请求增加浏览量），应使用 `GET /v1/admin/articles/{id}`（支持 draft/recycled，无副作用）
- ❌ **不要**绕过路由守卫直接访问管理页面
- ❌ **不要**在组件内直接 `localStorage`，用 `src/utils/storage.ts`

## NOTES

- 启动前**必须**设置 `DB_PASSWORD` 环境变量
- 数据库变更需同步 `sql/init.sql` 和 `doc/PRD.md`
- `.github/workflows/` 目录存在但为空，无CI/CD配置
- 测试覆盖率极低：8个测试类，前端无测试框架
- SSL证书使用自定义Python脚本 `rainyun_certbot.py` + 雨云DNS API自动续期
- `docker-compose.yml` 内嵌JWT_SECRET默认值（生产必须修改）
- Node 引擎要求：`^20.19.0 || >=22.12.0`

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | admin |
| `testuser` | `123456` | user |
