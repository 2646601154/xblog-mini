<!-- Generated: 2026-05-27 | Files scanned: 83 | Token estimate: ~300 -->

# Dependencies

## Backend (blog-api — Spring Boot 3.x, Java 17+)

| Dependency | Purpose |
|-----------|---------|
| spring-boot-starter-web | REST API 框架 |
| spring-boot-starter-data-redis | Redis 集成 |
| spring-security-crypto | BCrypt 密码加密 |
| mybatis-plus-boot-starter | ORM, CRUD 封装 |
| mysql-connector-j | MySQL 驱动 |
| jjwt (io.jsonwebtoken) | JWT 生成/解析 |
| knife4j (Swagger) | API 文档 |
| lombok | 代码简化 |
| aliyun-sdk-oss | 阿里云 OSS 文件上传 |

## Frontend (blog-admin)

| Dependency | Purpose |
|-----------|---------|
| vue 3 | UI 框架 |
| element-plus | 组件库 |
| pinia | 状态管理 |
| vue-router | 路由 |
| axios | HTTP 客户端 |

## Frontend (blog-web)

| Dependency | Purpose |
|-----------|---------|
| vue 3 | UI 框架 |
| element-plus | 组件库(轻量) |
| pinia | 状态管理 |
| axios | HTTP 客户端 |

## External Services

| Service | Use |
|---------|-----|
| MySQL | 主数据库 |
| Redis | 缓存/Token/Session |
| Alibaba Cloud OSS | 文件存储 (头像/封面图) |

## Infrastructure

| Path | Content |
|------|---------|
| `sql/` | 数据库脚本 |
| `docker/` | Docker Compose 部署 |
