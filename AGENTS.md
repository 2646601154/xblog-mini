# Xblog-mini AGENTS

## 项目概述

Xblog-mini 是一个轻量级个人博客系统，目前处于 **MVP 早期阶段**。项目采用多模块前后端分离架构：

- **blog-api**：后端 API 服务（Java 17 + Spring Boot 3.x）
- **blog-web**：前端展示端（Vue 3 + TypeScript + Vite + Pinia）
- **blog-admin**：前端管理后台（Vue 3 + TypeScript + Vite + Pinia）
- **blog-uniapp**：小程序端（规划中，尚未创建）

权威需求文档：`doc/PRD.md`（含完整架构、API、数据库设计）  
接口文档目录：`doc/api/`（auth.md / article.md / comment.md / category.md / tag.md / user.md / config.md）  
SQL 脚本：`sql/init.sql`（表结构）、`sql/test-data.sql`（测试数据）

---

## 技术栈与版本

| 模块 | 技术选型 | 版本 |
|------|----------|------|
| 后端 | Java | 17 |
| 后端 | Spring Boot | 3.5.14 |
| 后端 | MyBatis-Plus | 3.5.15 |
| 后端 | MySQL Connector/J | runtime（对应 MySQL 8.0） |
| 后端 | Redis | 7.x（通过 Lettuce 连接池） |
| 后端 | Lombok | 最新（optional） |
| 后端 | Validation | Spring Boot Starter |
| 前端 | Vue | 3.5.32 |
| 前端 | TypeScript | ~6.0.0 |
| 前端 | Vite | ^8.0.8 |
| 前端 | Pinia | ^3.0.4 |
| 前端 | Vue Router | ^5.0.4 |
| 构建 | Maven | 使用 `mvnw` 包装器 |
| 包管理 | pnpm | 前端使用（存在 `pnpm-lock.yaml`） |

---

## 项目结构

```
Xblog-mini/
├── blog-api/                      # 后端服务
│   ├── src/main/java/com/xblog/   # 主代码（包名固定为 com.xblog）
│   │   ├── Main.java              # Spring Boot 入口
│   │   ├── common/                # 公共枚举/工具（如 ResultCode）
│   │   ├── config/                # 配置类（当前为空，按 PRD 规划）
│   │   ├── controller/            # 控制器（当前为空）
│   │   ├── service/               # 服务层（当前为空）
│   │   ├── mapper/                # 数据访问层（当前为空）
│   │   ├── entity/                # 实体类（如 Result）
│   │   ├── dto/                   # 数据传输对象（如 QueryArticleDto）
│   │   ├── vo/                    # 视图对象（如 ArticleVo）
│   │   └── security/              # JWT/权限相关（当前为空）
│   ├── src/main/resources/
│   │   ├── application.yml        # 主配置文件
│   │   └── mapper/                # MyBatis XML 映射文件（当前为空）
│   ├── src/test/java/com/xblog/   # 测试代码
│   ├── pom.xml                    # Maven 构建配置
│   └── mvnw / mvnw.cmd            # Maven 包装器脚本
│
├── blog-web/                      # 前端展示端
│   ├── src/                       # Vue 源码
│   ├── package.json               # Node 依赖与脚本
│   ├── vite.config.ts
│   └── pnpm-lock.yaml
│
├── blog-admin/                    # 前端管理后台
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   └── pnpm-lock.yaml
│
├── doc/                           # 项目文档
│   ├── PRD.md                     # 产品需求文档（权威来源）
│   └── api/                       # 接口详细文档
│
└── sql/                           # 数据库脚本
    ├── init.sql                   # 建表语句
    └── test-data.sql              # 测试数据（含默认账号）
```

**当前代码状态（backend）**：backend 目前仅有骨架代码，包含统一的响应包装类 `Result<T>`、业务错误码枚举 `ResultCode`、以及空的 DTO/VO 示例。`controller`、`service`、`mapper`、`security`、`config` 等包已按 PRD 规划预留，但尚未填充实现。

---

## 构建与运行命令

### 后端（blog-api）

```bash
# 进入后端目录
cd blog-api

# 开发运行
./mvnw spring-boot:run

# 编译打包
./mvnw clean package

# 运行测试
./mvnw test
```

- 入口类：`com.xblog.Main`
- 默认端口：`8080`
- 上下文路径：`/`

### 前端

```bash
# blog-web 或 blog-admin
cd blog-web    # 或 cd blog-admin
pnpm install   # 或 npm install
pnpm dev       # 开发服务器
pnpm build     # 生产构建
```

- blog-web 开发端口：按 Vite 默认（通常是 `5173`）
- blog-admin 开发端口：按 Vite 默认（通常是 `5173`）

### 数据库初始化

```bash
# 在项目根目录执行
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

- 数据库名：`xblog`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`

---

## 配置说明

后端主配置文件：`blog-api/src/main/resources/application.yml`

**需要设置的环境变量**：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_PASSWORD` | MySQL root 密码 | **必填** |
| `REDIS_PASSWORD` | Redis 密码 | 空字符串 |
| `JWT_SECRET` | JWT 签名密钥 | `your-secret-key-change-in-production` |
| `OSS_ENDPOINT` | 阿里云 OSS Endpoint | 空 |
| `OSS_ACCESS_KEY_ID` | OSS AccessKeyId | 空 |
| `OSS_ACCESS_KEY_SECRET` | OSS AccessKeySecret | 空 |
| `OSS_BUCKET_NAME` | OSS Bucket 名称 | 空 |
| `OSS_URL_PREFIX` | OSS 访问 URL 前缀 | 空 |

**关键配置项**：

- **MySQL**：`jdbc:mysql://localhost:3306/xblog`
- **Redis**：`localhost:6379`，数据库 `0`
- **JWT**：算法为 **HS256**（注意：PRD 中写的是 RS256，实际代码配置为 HS256），有效期 **7 天**（`604800000` 毫秒）
- **文件上传限制**：单文件最大 `5MB`，单次请求最大 `20MB`
- **MyBatis-Plus**：
  - 逻辑删除字段：`deleted`
  - 逻辑删除值：`1`
  - 未删除值：`0`
  - 自动驼峰映射：`map-underscore-to-camel-case: true`
  - SQL 日志输出到控制台：`StdOutImpl`

---

## 代码组织规范

### 包名
- 统一根包名：**`com.xblog`**（不是 `com.xblog.xblog`）

### 分层目录（按 PRD 规划）

| 包名 | 职责 |
|------|------|
| `config` | Spring 配置类（CORS、拦截器、MyBatis-Plus 配置等） |
| `controller` | REST API 控制器 |
| `service` | 业务逻辑层 |
| `mapper` | MyBatis-Plus Mapper 接口 |
| `entity` | 数据库实体类 |
| `dto` | 请求/查询参数对象（Data Transfer Object） |
| `vo` | 响应视图对象（View Object） |
| `common` | 公共枚举、常量、工具类 |
| `security` | JWT 工具、认证过滤器、权限注解处理 |

### 响应格式

所有 API 返回统一包装类 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "errors": null
}
```

- 成功码固定为 `200`，消息固定为 `"success"`
- 错误时 `code` 使用 `ResultCode` 枚举值，`errors` 可用于字段级校验错误
- 分页数据结构：`{ records, total, page, size }`

### 业务错误码（ResultCode）

| 范围 | 模块 |
|------|------|
| 1000-1999 | 认证模块（登录失败、Token 过期/无效） |
| 2000-2999 | 用户模块（用户不存在、用户名已存在、用户被禁用） |
| 3000-3999 | 文章模块 |
| 4000-4999 | 评论模块 |
| 5000-5999 | 分类模块 |
| 6000-6999 | 标签模块 |
| 9000-9999 | 系统级错误 |

---

## 数据库设计规范

- **逻辑外键**：所有表保留外键字段（如 `category_id`、`author_id`），**不建立物理外键约束**，关系维护由应用层负责。
- **级联删除**：删除文章时，应用层需手动清理关联的评论和标签关联记录。
- **软删除**：**仅 `article` 表支持软删除**（`deleted` 字段，`0`=未删除，`1`=已删除）；其他表直接物理删除。
- **密码存储**：必须使用 BCrypt 加密，禁止明文存储。

### 现有表清单

1. `user` — 用户表（角色 `admin`/`user`，状态 `normal`/`disabled`）
2. `article` — 文章表（状态 `draft`/`published`/`recycled`，含软删除）
3. `category` — 分类表（删除前需校验是否关联文章）
4. `tag` — 标签表
5. `article_tag` — 文章标签关联表（复合主键）
6. `comment` — 评论表（状态 `pending`/`approved`/`rejected`）
7. `config` — 系统配置表（键值对）

---

## 缓存与 Redis 策略

- **Token 存储**：`user:token:{userId}`，有效期 7 天，支持后端主动失效
- **Token 黑名单**：`blacklist:token:{token}`，登出时写入，过期时间与 Token 剩余有效期一致
- **其他缓存场景**（规划中）：
  - `article:list:{page}:{size}` — 文章列表，5 分钟
  - `article:detail:{id}` — 文章详情，10 分钟
  - `category:list` / `tag:list` — 30 分钟
  - `config:{key}` — 1 小时
- 策略：**Cache Aside**，数据变更时主动删除对应缓存，TTL 作为兜底。

---

## 文件上传限制

| 类型 | 限制 |
|------|------|
| 单张图片 | 最大 5MB |
| 单次请求 | 最大 20MB |
| 允许的图片格式 | jpg、png、gif、webp |

生产环境图片/文件计划存储至 **阿里云 OSS**（当前配置项为占位符 TODO）。

---

## 安全与认证

- **认证方式**：JWT Bearer Token，请求头 `Authorization: Bearer <token>`
- **Token 算法**：HS256
- **Token 有效期**：7 天
- **权限角色**：
  - `guest`（未登录访客）：浏览已发布文章、已审核评论
  - `user`（普通用户）：访客权限 + 注册/登录 + 发表/管理自己的评论
  - `admin`（管理员）：所有权限 + 后台完整管理
- **密码加密**：BCrypt
- **SQL 注入防护**：MyBatis-Plus 参数化查询
- **XSS 防护**：文章正文为富文本 HTML，前端/后端需做对应过滤（待实现）

---

## 测试策略

### 当前状态
- 测试依赖已引入：`spring-boot-starter-test`、`mybatis-spring-boot-starter-test:3.0.5`
- 现有测试仅包含基础的上下文加载测试 `MainTests.java`
- **尚未编写业务单元测试和集成测试**

### 推荐测试结构（按 PRD 规划）

```
src/test/java/com/xblog/
├── MainTests.java           # 上下文加载
├── controller/              # Controller 层单元测试（MockMvc）
├── service/                 # Service 层单元测试（Mockito）
├── mapper/                  # Mapper 层测试（@MybatisTest）
└── integration/             # 集成测试
```

### 运行测试

```bash
cd blog-api
./mvnw test
```

---

## Git 规范

- **主分支**：`main`（MVP 阶段，暂不启用 `develop` 分支）
- **提交格式**：`<type>(<scope>): <subject>`
  - `type`：`feat`、`fix`、`docs`、`style`、`refactor`、`test`、`chore`
  - `scope`：模块名，如 `article`、`comment`、`auth`

---

## 测试账号

数据库测试数据（密码均为 `123456`，BCrypt 加密）：

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | admin |
| `testuser` | `123456` | user |
| `zhangsan` | `123456` | user |

---

## 部署说明（规划中）

PRD 中设计了基于 Docker Compose 的部署方案，包含：
- `nginx:alpine` — 反向代理 + 静态资源（80/443）
- `blog-api` — 后端服务（8080）
- `blog-web` — 前端展示端（3000）
- `blog-admin` — 前端管理端（3001）
- `mysql:8.0` — 数据库（3306）
- `redis:7-alpine` — 缓存（6379）

**注意**：Docker、docker-compose.yml 及相关配置文件目前尚未创建，仅在 PRD 中规划。

---

## 开发注意事项

1. **包名不可写错**：必须是 `com.xblog`，不是 `com.xblog.xblog`。
2. **数据库变更**：修改表结构后，需同步更新 `sql/init.sql` 和 `doc/PRD.md` 中的数据库设计章节。
3. **逻辑删除**：仅 `article` 表使用 MyBatis-Plus 逻辑删除，其他表物理删除。
4. **缓存一致性**：任何修改文章、分类、标签、配置的接口，完成后需清理对应 Redis 缓存。
5. **文件上传**：当前 OSS 配置为占位符，本地开发可先使用本地存储或 Mock。
6. **PRD 与实际代码的差异**：PRD 中 JWT 写为 RS256，但 `application.yml` 实际使用 HS256，以代码配置为准。
7. **接口版本**：所有 API 以 `/api/v1` 为前缀。
8. **AGENTS.md 语言**：本项目使用中文编写注释与文档，AI 编码时请保持中文。
