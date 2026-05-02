# Xblog-mini AGENTS

## 项目概述

Xblog-mini 是一个轻量级个人博客系统，目前处于 **MVP 早期阶段**。

- **blog-api**：后端 API 服务（Java 17 + Spring Boot 3.x）
- **blog-web**：前端展示端（Vue 3 + TypeScript + Vite + Pinia）
- **blog-admin**：前端管理后台（Vue 3 + TypeScript + Vite + Pinia）

**当前开发分支：`api`**（不是 `main`）

---

## 快速开始

### 后端（blog-api）

```bash
cd blog-api
$env:DB_PASSWORD = "123456"  # PowerShell
./mvnw spring-boot:run
```

- 入口类：`com.xblog.Main`
- 默认端口：`8080`
- **启动前必须设置环境变量 `DB_PASSWORD`**

### 前端

```bash
cd blog-web    # 或 cd blog-admin
pnpm install
pnpm dev
```

### 数据库初始化

```bash
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

---

## 关键规范

### 包名
- 统一根包名：**`com.xblog`**（不是 `com.xblog.xblog`）

### 依赖注入
- **Controller 和测试类使用 `@Resource`**（非 `@Autowired`）

### API 路径
- Controller 使用 `/v1/` 前缀（如 `/v1/admin/comments`）
- 前端通过 `/api/v1/` 访问，Nginx 反向代理重写

### 响应格式
```json
{ "code": 200, "message": "success", "data": {} }
```

### 业务错误码

| 范围 | 模块 |
|------|------|
| 1000-1999 | 认证模块 |
| 2000-2999 | 用户模块 |
| 3000-3999 | 文章模块 |
| 4000-4999 | 评论模块 |
| 5000-5999 | 分类模块 |
| 6000-6999 | 标签模块 |
| 9000-9999 | 系统级错误 |

---

## 关键踩坑点

### MyBatis-Plus 分页 null 安全
每个 `new Page<>(page, size)` 必须做 null 兜底：
```java
int pageNum = dto.getPage() != null ? dto.getPage() : 1;
int pageSize = dto.getSize() != null ? dto.getSize() : 10;
```

### 批量查询避免 N+1
评论模块示例：收集 `userId` → `selectBatchIds` → `Map<id, User>`

### 时间字段自动填充
`MybatisPlusFillMetaObjectHandler` 自动填充 `createdAt` 和 `updatedAt`，无需手动设置。

---

## 项目结构

```
blog-api/src/main/java/com/xblog/
├── controller/           # 公开接口（Article/Category/Tag/Config/Comment）
├── controller/admin/       # 管理端接口（User/Article/Category/Tag/Config/Comment）
├── dto/                   # 请求参数对象
├── entity/                # 数据库实体 + Result/PageResult
├── mapper/                # MyBatis-Plus Mapper
├── service/impl/          # 业务逻辑实现
├── vo/                    # 响应视图对象
└── common/                # 公共组件（ResultCode/UserContext/JwtInterceptor/...）
```

---

## 已实现模块状态

| 模块 | 公开接口 | 管理接口 |
|------|---------|---------|
| Auth | ✅ | - |
| User | - | ✅ |
| Article | ✅ | ✅ |
| Category | ✅ | ✅ |
| Tag | ✅ | ✅ |
| Comment | ✅ | ✅ |
| Config | ✅ | ✅ |

---

## 详细文档

- 权威需求：`doc/PRD.md`
- 接口文档：`doc/api/` 目录
- **API 开发指南**：`doc/api/AGENTS.md`（详细接口开发流程、代码模板、模块清单）
- SQL 脚本：`sql/init.sql`、`sql/test-data.sql`

---

## Git 规范

**提交格式**：`<type>(<scope>): <subject>`
- type：`feat`、`fix`、`docs`、`style`、`refactor`、`test`、`chore`
- scope：模块名，如 `article`、`comment`、`auth`

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | admin |
| `testuser` | `123456` | user |
| `zhangsan` | `123456` | user |

---

## 开发注意事项

1. 数据库变更需同步更新 `sql/init.sql` 和 `doc/PRD.md`
2. 逻辑删除：仅 `article` 表使用，其他表物理删除
3. 缓存一致性：修改配置后需清理对应 Redis 缓存
4. 本项目使用中文编写注释与文档，AI 编码时请保持中文