<!-- Generated: 2026-05-27 | Files scanned: 83 | Token estimate: ~600 -->

# Architecture

## Project Type

Monorepo — Spring Boot API + 3 frontend clients

```
xblog-mini/
├── blog-api/        Spring Boot 3.x REST API (port 8080)
├── blog-admin/      Vue 3 + Element Plus 管理后台
├── blog-web/        Vue 3 公开博客前端
├── blog-mobile/     WeChat 小程序
├── sql/             数据库脚本
└── docker/          Docker 部署
```

## Backend Layers (blog-api)

```
Controller → Service → Mapper (MyBatis-Plus) → MySQL
                  ↕
              RedisUtil → Redis (cache/session)
              JwtUtil   → JWT (access + refresh token)
              UserContext (ThreadLocal)
```

## Auth Flow

```
POST /v1/auth/login     → JWT AT(短期) + RT(7天,Redis)
POST /v1/auth/refresh   → RT rotation, 旧RT删除
POST /v1/auth/logout    → 删除所有RT, SCAN替代KEYS
POST /v1/auth/register  → BCrypt密码, 注册用户
GET  /v1/auth/me        → 当前登录用户
```

## Cache Strategy

| 键模式 | TTL | 用途 |
|--------|-----|------|
| `article:list:*` | 5min | 公开文章列表分页 |
| `article:detail:{id}` | 30min | 文章详情 |
| `article:tags:{id}` | 60min | 文章关联标签 |
| `article:view:{id}` | 24h | 浏览量去重(Set) |
| `config:public` | 10min | 公开配置 |
| `refresh_token:{uid}:{uuid}` | 7d | Refresh Token |

缓存操作使用 SCAN 命令（非阻塞），`@Deprecated` KEYS 方法。
空列表也缓存以防止缓存穿透。
标签/文章变更时主动失效相关缓存。
