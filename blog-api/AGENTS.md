# Repository Guidelines

## Project Overview

`blog-api` 是 Xblog-mini 的 Java/Spring Boot 后端模块，用于提供轻量级个人博客系统的公开接口和管理端接口。核心能力包括文章、评论、分类、标签、用户、配置、媒体上传、认证授权和仪表盘统计。

## Architecture & Data Flow

- 技术栈：Java 17、Spring Boot 3.5.14、MyBatis-Plus 3.5.15、MySQL 8、Redis、JWT、Lombok、Knife4j/SpringDoc、阿里云 OSS。
- 分层结构保持既有模式：`Controller -> Service 接口 -> ServiceImpl -> Mapper -> Entity`。
- 典型请求链路：
  1. HTTP 请求进入 `/v1/**`。
  2. `WebMvcConfig` 注册 `JwtInterceptor`，并通过白名单放行公开接口。
  3. `JwtInterceptor` 解析 `Authorization: Bearer <token>`，将当前用户写入 `UserContext` ThreadLocal。
  4. Controller 使用 Jakarta Validation 校验 DTO，调用 Service，返回 `Result<T>`。
  5. Service 使用 MyBatis-Plus Wrapper 或 Mapper `@Select` SQL 查询数据，手动组装 VO；分页结果返回 `PageResult<T>`。
  6. `afterCompletion` 清理 `UserContext`，避免 ThreadLocal 泄漏。
- 认证采用双 Token：短期 JWT Access Token + 存储在 Redis 的 UUID Refresh Token。
- Redis 用于 Refresh Token 和部分 cache-aside 业务缓存，文章模块是主要示例。
- 文件上传/删除通过 `OssUtil` 对接阿里云 OSS。
- `GlobalExceptionHandler` 统一处理 `BusinessException`、JWT 异常、参数校验异常和兜底异常。

## Key Directories

- `src/main/java/com/xblog/controller/` — 公开 REST Controller。
- `src/main/java/com/xblog/controller/admin/` — 管理端 REST Controller；与公开 Controller 同名时使用显式 Bean 名，如 `@RestController("adminUserController")`。
- `src/main/java/com/xblog/service/`、`src/main/java/com/xblog/service/impl/` — 业务接口和实现。
- `src/main/java/com/xblog/mapper/` — MyBatis-Plus `BaseMapper`；自定义 SQL 使用注解方式，不使用 XML Mapper。
- `src/main/java/com/xblog/entity/` — 数据库实体和通用响应对象（`Result`、`PageResult`）。
- `src/main/java/com/xblog/dto/` — 请求/查询对象，常见命名为 `XxxParam`、`XxxCreateParam`、`XxxUpdateParam`、`QueryXxxDto`。
- `src/main/java/com/xblog/vo/` — 响应视图对象。
- `src/main/java/com/xblog/common/` — 配置、枚举、异常、属性、拦截器、工具类。
- `src/main/java/com/xblog/handler/` — 全局异常处理和 MyBatis-Plus 字段填充处理器。
- `src/main/resources/` — 应用配置。
- `src/test/java/com/xblog/` — JUnit 5 测试代码。

## Development Commands

优先使用 Maven Wrapper，不要求系统 Maven 版本一致。

```bash
# 构建但跳过测试
./mvnw clean package -DskipTests

# 运行测试
./mvnw test

# 本地启动应用
./mvnw spring-boot:run

# Windows 等价命令
mvnw.cmd test
mvnw.cmd spring-boot:run

# 构建 Docker 镜像
docker build -t blog-api .
```

完整启动和部分集成测试需要可用的 MySQL 与 Redis。Docker 运行时还需要提供 `src/main/resources/application.yml` 中声明的环境变量。

## Code Conventions & Common Patterns

- Spring 组件优先使用构造器注入；Controller 保持轻量，业务规则放在 Service 层。
- 保留历史包名 `common.intercepter`，不要随手改成 `interceptor`，否则会影响已有导入和配置。
- 代码中的接口路径使用 `/v1/...`；部署或代理层可能对外暴露 `/api/v1/...`。
- 新增无需登录的公开接口时，必须同步更新 `WebMvcConfig.excludePathPatterns(...)` 白名单。
- API 成功响应使用 `Result.success(...)`；业务失败抛出携带 `ResultCode` 的 `BusinessException`。
- 新增业务错误码时遵循 `ResultCode` 既有分段：
  - `1000` 认证，`2000` 用户，`3000` 文章，`4000` 评论，`5000` 分类，`6000` 标签，`9000` 系统。
- 保持 DTO/Entity/VO 分离。面向前端的 VO 使用显式字段赋值，避免自动复制导致密码或敏感字段泄漏。
- 分页必须做 null 安全兜底。优先使用 `PageUtil`，或在创建 `Page<>` 前明确默认 `page=1`、`size=10`。
- 避免 N+1 查询。先收集关联 ID，再使用 `selectBatchIds` 等批量查询方式组装 VO。
- Redis 缓存 key 和 TTL 通常定义在 ServiceImpl 中；修改会影响缓存的数据时，在源头清理相关缓存。
- `UserContext` 仅用于请求级 ThreadLocal 状态，不要跨请求保存或异步复用。
- `ResultCode.sensitive=true` 表示敏感错误，客户端文案会被脱敏，服务端保留日志细节。
- Entity 使用 Lombok 和 MyBatis-Plus 注解；`Article` 使用 `@TableLogic` 做逻辑删除。
- `createdAt`/`updatedAt` 由 `MybatisPlusFillMetaObjectHandler` 自动填充。

## Important Files

- `src/main/java/com/xblog/Main.java` — Spring Boot 启动入口。
- `pom.xml` — 依赖、插件、Java/Spring/Maven 配置来源。
- `Dockerfile` — Maven 构建 + Temurin 17 运行时的两阶段镜像。
- `src/main/resources/application.yml` — 主配置、环境变量、上传限制、Knife4j/SpringDoc 分组。
- `src/main/resources/application-local.yml` — 本地配置文件；如存在通常包含本地密钥，已被 gitignore 忽略。
- `src/main/java/com/xblog/common/config/WebMvcConfig.java` — JWT 拦截器注册和公开路由白名单。
- `src/main/java/com/xblog/common/intercepter/JwtInterceptor.java` — Bearer Token 解析和 `UserContext` 生命周期。
- `src/main/java/com/xblog/common/enums/ResultCode.java` — API/业务错误码契约。
- `src/main/java/com/xblog/handler/GlobalExceptionHandler.java` — 统一错误响应行为。
- `src/main/java/com/xblog/common/util/PageUtil.java` — 分页默认值和转换工具。
- `src/main/java/com/xblog/common/util/RedisUtil.java` — Redis 工具，包括基于 SCAN 的模式删除。
- `src/main/java/com/xblog/common/util/OssUtil.java` — 阿里云 OSS 上传/删除逻辑。
- `src/main/java/com/xblog/service/impl/ArticleServiceImpl.java` — 最大 Service，展示文章查询、缓存和状态流转模式。
- `src/main/java/com/xblog/service/impl/UserServiceImpl.java` — 登录、Refresh Token、BCrypt 密码和用户 VO 转换模式。
- `src/main/java/com/xblog/service/impl/CommentServiceImpl.java` — 评论状态流转和批量关联加载模式。

## Runtime/Tooling Preferences

- 必需运行时：Java 17。
- 构建工具：Maven Wrapper（`./mvnw` 或 `mvnw.cmd`）。
- 默认端口：`8080`。
- API 文档由 Knife4j/SpringDoc 运行时生成：
  - Knife4j UI：`http://localhost:8080/doc.html`
  - OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 主要环境变量：
  `SPRING_DATASOURCE_URL`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`JWT_SECRET`、`OSS_ENDPOINT`、`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET`、`OSS_BUCKET_NAME`、`OSS_URL_PREFIX`。
- 当前模块未配置 Flyway/Liquibase；数据库 Schema 由外部 SQL 或部署环境管理。
- 当前没有项目级 lint/format 命令。修改 Java 代码时遵循既有风格，避免无关格式化。

## Testing & QA

- 测试框架：JUnit 5（`spring-boot-starter-test`）、Mockito、Spring Test，必要时直接使用 Bean Validation。
- 运行测试：`./mvnw test`（Windows 使用 `mvnw.cmd test`）。
- 已有单元测试覆盖部分工具类、DTO 和 Service，如 `OssUtil`、`UpdateUserParam`、`ResultCode` 脱敏、`DashboardServiceImpl`、用户 VO 密码排除。
- 部分 `@SpringBootTest` 依赖真实 MySQL/Redis；当前没有 Testcontainers、H2 或 `application-test.yml`。
- 新增业务逻辑时优先写聚焦的 Mockito/单元测试；只有确实需要 Spring 容器时再使用全量上下文测试。
- Controller 行为建议优先补 Spring MVC slice 测试，而不是直接堆全量集成测试。
- 修改认证、分页、错误处理、缓存失效或 VO 映射时，应补充回归测试，断言业务行为而不是内部实现细节。
