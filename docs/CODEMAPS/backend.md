<!-- Generated: 2026-05-27 | Files scanned: 83 | Token estimate: ~800 -->

# Backend

## API Routes

### Public (prefix: `/v1`)

| Method | Path | Controller | Service Method |
|--------|------|-----------|---------------|
| GET | `/v1/articles` | ArticleController | getPublicArticlePage |
| GET | `/v1/articles/{id}` | ArticleController | getArticleDetail |
| GET | `/v1/articles/{id}/prev-next` | ArticleController | getArticlePrevNext |
| GET | `/v1/articles/{id}/tags` | ArticleController | getArticleTags |
| GET | `/v1/articles/{id}/comments` | ArticleController | getArticleComments |
| GET | `/v1/categories` | CategoryController | getPublicCategories |
| GET | `/v1/tags` | TagController | getPublicTagList |
| GET | `/v1/config` | ConfigController | getPublicConfig |
| POST | `/v1/auth/login` | AuthController | login |
| POST | `/v1/auth/refresh` | AuthController | refreshAccessToken |
| POST | `/v1/auth/logout` | AuthController | logout |
| POST | `/v1/auth/register` | AuthController | register |
| GET | `/v1/auth/me` | AuthController | getLoginUser |
| GET | `/v1/user/profile` | UserController | getProfile |
| PUT | `/v1/user/profile` | UserController | updateProfile |
| PUT | `/v1/user/password` | UserController | updatePassword |
| POST | `/v1/comments` | CommentController | createComment |
| GET | `/v1/comments/my` | CommentController | getMyComments |

### Admin (prefix: `/v1/admin`)

| Method | Path | Controller | Service Method |
|--------|------|-----------|---------------|
| GET | `/v1/admin/articles` | admin.ArticleController | getAdminArticlePage |
| GET | `/v1/admin/articles/{id}` | admin.ArticleController | getAdminArticleDetail |
| POST | `/v1/admin/articles` | admin.ArticleController | createArticle |
| PUT | `/v1/admin/articles/{id}` | admin.ArticleController | updateArticle |
| PUT | `/v1/admin/articles/{id}/publish` | admin.ArticleController | publishArticle |
| PUT | `/v1/admin/articles/{id}/recycle` | admin.ArticleController | recycleArticle |
| PUT | `/v1/admin/articles/{id}/restore` | admin.ArticleController | restoreArticle |
| DELETE | `/v1/admin/articles/{id}` | admin.ArticleController | deleteArticle |
| POST | `/v1/admin/articles/{id}/tags` | admin.ArticleController | bindTags |
| GET/POST | `/v1/admin/categories` | admin.CategoryController | CRUD |
| GET/POST | `/v1/admin/tags` | admin.TagController | CRUD |
| PUT | `/v1/admin/tags/{id}` | admin.TagController | updateTag |
| DELETE | `/v1/admin/tags/{id}` | admin.TagController | deleteTag |
| GET | `/v1/admin/comments` | admin.CommentController | list/update/delete |
| GET | `/v1/admin/users` | admin.UserController | getUserPage |
| POST | `/v1/admin/users` | admin.UserController | createUser |
| PUT | `/v1/admin/users/{id}` | admin.UserController | updateUser |
| PUT | `/v1/admin/users/{id}/disable` | admin.UserController | disableUser |
| PUT | `/v1/admin/users/{id}/enable` | admin.UserController | enableUser |
| DELETE | `/v1/admin/users/{id}` | admin.UserController | deleteUser |
| PUT | `/v1/admin/users/{id}/reset-password` | admin.UserController | resetPassword |
| GET | `/v1/admin/config` | admin.ConfigController | getAllConfigs |
| PUT | `/v1/admin/config` | admin.ConfigController | updateConfigs |
| GET | `/v1/admin/dashboard` | admin.DashboardController | getDashboard |
| POST | `/v1/admin/upload` | admin.UploadController | uploadFile |

## Middleware Chain

```
Request → JwtInterceptor → Controller → Service → Mapper → DB
            ↓
      验证JWT → 设置UserContext → 可选角色检查
```

## Service → Mapper Map

| Service | Mapper(s) | Key Operations |
|---------|-----------|----------------|
| ArticleServiceImpl | ArticleMapper, CategoryMapper, UserMapper, TagMapper, ArticleTagMapper | CRUD, 发布/回收/恢复, 标签绑定, Redis缓存 |
| UserServiceImpl | UserMapper | 登录/注册, JWT+RT管理, CRUD, 密码重置 |
| CommentServiceImpl | CommentMapper | 评论CRUD, 审核 |
| CategoryServiceImpl | CategoryMapper | 分类CRUD |
| TagServiceImpl | TagMapper, ArticleTagMapper | 标签CRUD, 缓存失效 |
| ConfigServiceImpl | ConfigMapper | 配置查询/更新, Redis缓存 |
| DashboardServiceImpl | (多Mapper) | 统计仪表盘 |

## Key Files

- `service/impl/ArticleServiceImpl.java` (795 lines) — 最复杂的服务, 含完整缓存层
- `service/impl/UserServiceImpl.java` (399 lines) — JWT双Token认证
- `common/util/RedisUtil.java` (377 lines) — Redis工具, SCAN/Set/Hash/DistLock
- `common/util/JwtUtil.java` — JWT生成解析
- `common/intercepter/JwtInterceptor.java` — 认证拦截器
- `common/config/RedisConfig.java` — GenericJackson2Json序列化配置
- `handler/GlobalExceptionHandler.java` — 全局异常处理
