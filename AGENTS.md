# Xblog-mini PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-19
**Branch:** api

## OVERVIEW

轻量级个人博客系统（MVP）。3模块：Java/Spring Boot后端 + Vue3前台 + Vue3管理后台。

## STRUCTURE

```
Xblog-mini/
├── blog-api/          # Java 17 + Spring Boot 3.x 后端（105个Java文件，18个包）
├── blog-web/          # Vue 3 + TypeScript + Vite 前台（Element Plus）
├── blog-admin/        # Vue 3 + TypeScript + Vite 管理后台（Tailwind CSS v4）
├── docker/            # Docker多阶段构建（api/web/admin/nginx各自独立Dockerfile）
├── doc/api/           # API接口文档
├── sql/               # 数据库初始化脚本
└── AGENTS.md          # 本文件
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 后端开发规范 | `blog-api/AGENTS.md` | 286行，详细接口开发流程 |
| 前端API调用 | `blog-web/src/api/` / `blog-admin/src/api/` | Axios封装 + 模块化endpoint |
| 前端状态管理 | `blog-web/src/stores/` / `blog-admin/src/stores/` | Pinia composition API |
| 前端路由 | `blog-web/src/router/` / `blog-admin/src/router/` | Vue Router + 路由守卫 |
| 组件库 | `blog-web/src/components/` / `blog-admin/src/components/` | 复用组件 |
| 业务错误码 | `blog-api/src/main/java/com/xblog/common/enums/ResultCode.java` | 错误码定义 |

## CODE MAP

| Symbol | Type | Location | Hotspot |
|--------|------|----------|---------|
| `ArticleServiceImpl` | Service | `blog-api/.../service/impl/` | 494行，最大Service |
| `CommentServiceImpl` | Service | `blog-api/.../service/impl/` | 284行，N+1查询典范 |
| `RedisUtil` | Util | `blog-api/.../common/util/` | 270行，Redis操作封装 |
| `detail.vue` | Vue | `blog-web/src/views/article/` | 341行，文章详情页 |

## ANTI-PATTERNS（THIS PROJECT）

- ❌ **不要**在 `node_modules` 目录放置项目文件（已发现误提交）
- ❌ **不要**使用 `@Autowired`，用 `@Resource`
- ❌ **不要**忘记分页null兜底：`int pageNum = dto.getPage() != null ? dto.getPage() : 1`
- ❌ **不要**在循环内单独查询关联数据，用 `selectBatchIds` 批量查询
- ❌ **不要**修改配置后忘记清理Redis缓存
- ❌ **不要**在 `application.yml` 中使用默认/占位密钥（JWT secret 当前为 `your-secret-key-change-in-production`）
- ❌ **不要**忘记清理 ThreadLocal（`UserContext` 使用 ThreadLocal 存储用户信息，需确保在请求结束后调用 `clear()`）
- ❌ **不要**提交嵌套 `blog-web/blog-web/` 目录（疑似构建产物混淆）

## UNIQUE STYLES

- 后端入口类：`com.xblog.Main`（非典型命名）
- 两套独立前端（blog-web/blog-admin），非workspace单仓库结构
- 后端无root聚合pom.xml，三模块独立构建
- `blog-admin` 使用 Tailwind CSS v4 + `@tailwindcss/vite` 插件（`blog-web` 使用 Element Plus）
- 前端无项目级 `.eslintrc` / `.prettierrc`（仅使用 IDE 默认规则）

## CI/CD

- **.github/workflows/**: 当前为空，无活跃 CI 工作流
- **mvnw**: blog-api包含Maven wrapper，但CI未使用
- **Docker**: 存在两套Dockerfile（`blog-api/Dockerfile` vs `docker/Dockerfile.api`），`docker-compose` 使用后者

## TESTS

- 仅3个集成测试（`DemoTest`, `MainTests`, `TestUserServiceImpl`），全量Spring上下文
- 无单元测试、无Mock测试
- 无JaCoCo覆盖率配置
- 测试数据：`sql/test-data.sql`

## COMMANDS

```bash
# 后端启动
cd blog-api
$env:DB_PASSWORD = "123456"
./mvnw spring-boot:run

# 前端启动
cd blog-web    # 或 blog-admin
pnpm install
pnpm dev

# Docker部署
cd docker
docker-compose up -d --build

# 数据库
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

## NOTES

- 启动前**必须**设置 `DB_PASSWORD` 环境变量
- `MybatisPlusFillMetaObjectHandler` 自动填充 `createdAt`/`updatedAt`
- 逻辑删除：仅 `article` 表使用，其他表物理删除
- 数据库变更需同步 `sql/init.sql` 和 `doc/PRD.md`
- `blog-web/blog-web/` 子目录异常，需确认是否为构建产物
- `docker/dist-web/`、`docker/dist-admin/`、`docker/blog-api-0.0.1-SNAPSHOT.jar` 为构建产物，不应提交到仓库
- 存在空的 `com.xblog.xblog` 包目录，无实际用途

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | admin |
| `testuser` | `123456` | user |
| `zhangsan` | `123456` | user |
