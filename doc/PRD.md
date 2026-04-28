# Xblog-mini 产品需求文档 (PRD)

## 1. 项目概述

| 项目 | 说明 |
|------|------|
| **项目名称** | Xblog-mini |
| **项目定位** | 轻量级个人博客系统，适用于个人博主搭建技术博客 |
| **核心功能** | 文章发布与管理、分类标签、评论互动、用户认证 |
| **目标用户** | 个人博主及其读者 |
| **版本** | v1.0.0 |

### 技术栈

| 端 | 技术选型 |
|----|----------|
| 前端展示端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router |
| 前端管理端 | Vue 3 + TypeScript + Vite + Element Plus |
| 小程序端 | UniApp + Vue 3 |
| 后端服务 | Java 17 + Spring Boot 3.x + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x |
| 对象存储 | 阿里云 OSS |
| 部署 | Docker + Docker Compose + Nginx |

---

## 2. 系统架构

### 2.1 架构图

```
                              ┌─────────────────────────────────────┐
                              │            Nginx 网关               │
                              │         (端口 80/443)               │
                              └──────────┬──────────────┬───────────┘
                                         │              │
                    ┌────────────────────┘              └────────────────────┐
                    ▼                                                          ▼
          ┌─────────────────┐                                        ┌─────────────────┐
          │   前端展示端     │                                        │   前端管理端     │
          │   blog-web      │                                        │   blog-admin    │
          │   端口 3000     │                                        │   端口 3001     │
          └────────┬────────┘                                        └────────┬────────┘
                   │                                                           │
                   └───────────────────────┬───────────────────────────┘
                                           │ REST API (JSON)
                                           ▼
                              ┌─────────────────────────────────────┐
                              │          Spring Boot API            │
                              │           端口 8080                 │
                              └──────────┬──────────────┬───────────┘
                                         │              │
                         ┌───────────────┘              └───────────────┐
                         ▼                                                  ▼
               ┌─────────────────┐                                  ┌─────────────────┐
               │     MySQL 8     │                                  │     Redis 7      │
               │   端口 3306     │                                  │   端口 6379     │
               └─────────────────┘                                  └─────────────────┘
                                                                            │
                                                                            ▼
                                                            ┌─────────────────────────┐
                                                            │      阿里云 OSS         │
                                                            │   (图片/文件存储)       │
                                                            └─────────────────────────┘
```

### 2.2 小程序端架构

```
┌─────────────────────────────────────┐
│          微信小程序 / 支付宝小程序      │
│           blog-uniapp               │
│                                     │
│  ├── 文章列表 (分页加载)             │
│  ├── 文章详情 (富文本展示)            │
│  ├── 分类/标签筛选                  │
│  └── 评论列表查看 (仅已审核评论)      │
└─────────────────────────────────────┘
                │
                │ HTTP REST API
                ▼
      ┌─────────────────┐
      │  Spring Boot    │
      │      API        │
      └─────────────────┘
```

---

## 3. 用户角色与权限

| 角色 | 标识 | 权限范围 |
|------|------|----------|
| **访客** | guest | 浏览已发布文章、查看已审核评论 |
| **普通用户** | user | 访客权限 + 注册/登录 + 发表/管理自己的评论（需审核） |
| **管理员** | admin | 所有权限 + 后台完整管理功能 |

### 权限矩阵

| 功能 | 访客 | 普通用户 | 管理员 |
|------|------|----------|--------|
| 浏览文章列表 | ✓ | ✓ | ✓ |
| 阅读文章详情 | ✓ | ✓ | ✓ |
| 查看评论 | ✓ | ✓ | ✓ |
| 注册/登录 | - | ✓ | ✓ |
| 发表评论 | - | ✓ | ✓ |
| 管理自己的评论 | - | ✓ | - |
| 管理所有评论 | - | - | ✓ |
| 发布/编辑文章 | - | - | ✓ |
| 管理分类/标签 | - | - | ✓ |
| 管理用户 | - | - | ✓ |
| 系统配置 | - | - | ✓ |

---

## 4. 功能模块详述

### 4.1 文章模块

#### 4.1.1 文章状态

| 状态 | 标识 | 说明 |
|------|------|------|
| 草稿 | `draft` | 正在编辑，暂不公开 |
| 已发布 | `published` | 正式发布，所有人可见 |
| 回收站 | `recycled` | 软删除，可恢复或彻底删除 |

#### 4.1.2 文章属性

| 属性 | 类型 | 说明 |
|------|------|------|
| 标题 | String | 最多 200 字符 |
| 摘要 | String | 最多 500 字符，可自动提取或手动填写 |
| 正文 | Text | 富文本内容 (HTML) |
| 封面图 | String | OSS URL，可选 |
| 分类 | FK | 必选，归属一个分类 |
| 标签 | Array | 可选，0-N 个标签 |
| 作者 | FK | 关联用户表 |
| 状态 | Enum | draft/published/recycled |
| 浏览量 | Integer | 整数，默认 0 |
| 创建时间 | DateTime | 自动生成 |
| 发布时间 | DateTime | 发布时自动设置 |
| 更新时间 | DateTime | 修改时自动更新 |

#### 4.1.3 富文本编辑器

- **选型**：Tiptap 2.x
- **功能**：
  - 标题 (H1-H3)
  - 粗体、斜体、删除线
  - 有序/无序列表
  - 引用块
  - 代码块 (支持语法高亮)
  - 图片上传 (直接上传至 OSS)
  - 链接
  - 水平分割线

### 4.2 评论模块

#### 4.2.1 评论状态

| 状态 | 标识 | 说明 |
|------|------|------|
| 待审核 | `pending` | 提交后默认状态 |
| 已通过 | `approved` | 管理员审核通过后显示 |
| 已驳回 | `rejected` | 管理员审核驳回 |

#### 4.2.2 评论属性

| 属性 | 类型 | 说明 |
|------|------|------|
| 文章 | FK | 关联文章表 |
| 用户 | FK | 评论者，关联用户表 |
| 内容 | String | 评论内容，最多 1000 字符 |
| 状态 | Enum | pending/approved/rejected |
| 创建时间 | DateTime | 自动生成 |

#### 4.2.3 评论规则

- **发表评论**：仅登录用户可发表
- **审核机制**：评论默认待审核，管理员审核后展示
- **显示规则**：文章详情页仅展示 `approved` 状态的评论
- **排序**：按创建时间倒序 (最新在前)

### 4.3 用户模块

#### 4.3.1 用户属性

| 属性 | 类型 | 说明 |
|------|------|------|
| 用户名 | String | 唯一，登录凭证，3-20 字符，字母开头 |
| 密码 | String | BCrypt 加密存储，最少 6 字符 |
| 昵称 | String | 显示名称，可重复，2-50 字符 |
| 头像 | String | OSS URL，可选 |
| 邮箱 | String | 唯一，可选，用于通知 |
| 角色 | Enum | admin/user |
| 状态 | Enum | normal/disabled |
| 创建时间 | DateTime | 自动生成 |
| 更新时间 | DateTime | 修改时自动更新 |

#### 4.3.2 认证机制

- **登录方式**：用户名 + 密码
- **Token**：JWT (RS256 算法)
- **Token 时长**：7 天
- **Token 存储**：Redis，支持后端主动失效
- **注册**：开放注册，默认角色为 `user`

### 4.4 分类模块

| 属性 | 类型 | 说明 |
|------|------|------|
| 名称 | String | 分类名称，唯一，2-20 字符 |
| slug | String | URL 友好标识，唯一 |
| 描述 | String | 可选，最多 200 字符 |
| 排序 | Integer | 数字越小越靠前 |
| 创建时间 | DateTime | 自动生成 |

**删除限制：** 有文章的分类禁止删除

### 4.5 标签模块

| 属性 | 类型 | 说明 |
|------|------|------|
| 名称 | String | 标签名称，唯一，1-20 字符 |
| slug | String | URL 友好标识，唯一 |
| 创建时间 | DateTime | 自动生成 |

### 4.6 系统配置模块

| 配置项 | Key | 说明 |
|--------|-----|------|
| 网站标题 | site_title | 浏览器标签页标题 |
| 网站 Logo | site_logo | OSS URL |
| 网站描述 | site_description | SEO meta description |
| 备案号 | icp_number | 页面底部显示 |
| 版权信息 | copyright | 页面底部显示 |
| 管理员用户名 | admin_username | 初始管理员用户名 |
| 管理员密码 | admin_password | 初始管理员密码 (需加密) |

### 4.7 文件上传限制

| 类型 | 限制 |
|------|------|
| 图片大小 | 单张不超过 5MB |
| 图片类型 | jpg, png, gif, webp |
| 文件大小 | 单个文件不超过 20MB |

---

## 5. 数据库设计

### 5.1 设计原则

| 原则 | 说明 |
|------|------|
| **逻辑外键** | 保留外键字段，但不建物理外键约束，由应用层维护关系 |
| **级联处理** | 删除文章时，由应用层手动删除关联的评论和标签 |
| **软删除** | 仅文章表支持软删除，评论表直接物理删除 |

### 5.2 ER 图概要

```
user (1) ──────< (N) article
  │                    │
  │                    │
  │              (N) ──┘
  │                    │
  └── (1) < comment >──┘
           (N) │
               │
         category (1) ──< (N) article
               │
         tag (N) ──< article_tag >─< (N) article
```

### 5.3 表结构

#### 5.3.1 用户表 (user)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| username | VARCHAR(20) | NOT NULL, UNIQUE | 用户名，3-20 字符 |
| password | VARCHAR(255) | NOT NULL | 密码 (BCrypt)，最少 6 字符 |
| nickname | VARCHAR(50) | NOT NULL | 昵称，2-50 字符 |
| avatar | VARCHAR(500) | NULL | 头像 URL |
| email | VARCHAR(100) | NULL, UNIQUE | 邮箱，唯一 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'user' | 角色: admin/user |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'normal' | 状态: normal/disabled |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

```sql
CREATE TABLE user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(20) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    nickname      VARCHAR(50) NOT NULL,
    avatar        VARCHAR(500) DEFAULT NULL,
    email         VARCHAR(100) DEFAULT NULL UNIQUE,
    role          VARCHAR(20) NOT NULL DEFAULT 'user',
    status        VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

**索引：** `idx_username` (username)

---

#### 5.3.2 文章表 (article)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| title | VARCHAR(200) | NOT NULL | 文章标题 |
| summary | VARCHAR(500) | NULL | 文章摘要 |
| content | LONGTEXT | NOT NULL | 文章正文 (HTML) |
| cover_image | VARCHAR(500) | NULL | 封面图 URL |
| category_id | BIGINT | NOT NULL | 分类 ID (逻辑外键) |
| author_id | BIGINT | NOT NULL | 作者 ID (逻辑外键) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'draft' | 状态: draft/published/recycled |
| view_count | INT | NOT NULL, DEFAULT 0 | 浏览量 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| published_at | DATETIME | NULL | 发布时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除: 0-未删除 1-已删除 |

```sql
CREATE TABLE article (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(200) NOT NULL,
    summary       VARCHAR(500) DEFAULT NULL,
    content       LONGTEXT NOT NULL,
    cover_image   VARCHAR(500) DEFAULT NULL,
    category_id   BIGINT NOT NULL,
    author_id     BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'draft',
    view_count    INT NOT NULL DEFAULT 0,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at  DATETIME DEFAULT NULL,
    deleted       TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
```

**索引：**
- `idx_status_published_at` (status, published_at) — 列表查询
- `idx_category_id` (category_id) — 分类筛选
- `idx_author_id` (author_id) — 作者查询

---

#### 5.3.3 分类表 (category)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| name | VARCHAR(20) | NOT NULL, UNIQUE | 分类名称 |
| slug | VARCHAR(50) | NOT NULL, UNIQUE | URL 标识 |
| description | VARCHAR(200) | NULL | 分类描述 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序 (越小越靠前) |
| created_at | DATETIME | NOT NULL | 创建时间 |

```sql
CREATE TABLE category (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(20) NOT NULL UNIQUE,
    slug          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(200) DEFAULT NULL,
    sort_order    INT NOT NULL DEFAULT 0,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';
```

**索引：** `idx_sort_order` (sort_order)

**删除限制：** 禁止删除有文章的分类（应用层校验）

---

#### 5.3.4 标签表 (tag)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| name | VARCHAR(20) | NOT NULL, UNIQUE | 标签名称 |
| slug | VARCHAR(50) | NOT NULL, UNIQUE | URL 标识 |
| created_at | DATETIME | NOT NULL | 创建时间 |

```sql
CREATE TABLE tag (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(20) NOT NULL UNIQUE,
    slug          VARCHAR(50) NOT NULL UNIQUE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
```

---

#### 5.3.5 文章标签关联表 (article_tag)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| article_id | BIGINT | NOT NULL, PK | 文章 ID |
| tag_id | BIGINT | NOT NULL, PK | 标签 ID |

```sql
CREATE TABLE article_tag (
    article_id    BIGINT NOT NULL,
    tag_id        BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';
```

**索引：**
- `idx_tag_id` (tag_id) — 按标签查询文章

---

#### 5.3.6 评论表 (comment)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| article_id | BIGINT | NOT NULL | 文章 ID (逻辑外键) |
| user_id | BIGINT | NOT NULL | 用户 ID (逻辑外键) |
| content | VARCHAR(1000) | NOT NULL | 评论内容 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'pending' | 状态: pending/approved/rejected |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

```sql
CREATE TABLE comment (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id    BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    content       VARCHAR(1000) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

**索引：**
- `idx_article_id_status` (article_id, status) — 文章评论列表
- `idx_user_id` (user_id) — 我的评论查询

---

#### 5.3.7 系统配置表 (config)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| config_key | VARCHAR(100) | NOT NULL, UNIQUE | 配置键 |
| config_value | TEXT | NULL | 配置值 |
| description | VARCHAR(200) | NULL | 配置描述 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

```sql
CREATE TABLE config (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key    VARCHAR(100) NOT NULL UNIQUE,
    config_value  TEXT DEFAULT NULL,
    description   VARCHAR(200) DEFAULT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
```

---

## 6. API 设计规范

### 6.1 基础规范

- **基础路径**：`/api/v1`
- **协议**：HTTP/HTTPS
- **请求格式**：JSON (`Content-Type: application/json`)
- **认证方式**：Bearer Token (JWT)

### 6.2 响应格式

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

#### 分页响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [],
        "total": 100,
        "page": 1,
        "size": 10
    }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码 |
| message | String | 消息 |
| data.records | Array | 数据列表 |
| data.total | Integer | 总记录数 |
| data.page | Integer | 当前页码 |
| data.size | Integer | 每页条数 |

#### 错误响应

```json
{
    "code": 400,
    "message": "请求参数错误",
    "errors": {
        "username": "用户名已存在"
    }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | HTTP 状态码 |
| message | String | 错误描述 |
| errors | Object | 字段级错误（可选） |

### 6.3 HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 请求无效 |
| 401 | 未认证 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 6.4 分页参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 1 | 页码 |
| size | Integer | 10 | 每页条数 |

### 6.6 主要 API 列表

#### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/articles | 文章列表（分页，仅已发布） |
| GET | /api/v1/articles/{id} | 文章详情 |
| GET | /api/v1/categories | 分类列表 |
| GET | /api/v1/tags | 标签列表 |
| GET | /api/v1/articles/{id}/comments | 评论列表（仅已审核） |
| POST | /api/v1/auth/login | 用户登录 |
| POST | /api/v1/auth/register | 用户注册 |
| GET | /api/v1/configs | 获取系统配置（公开部分） |

#### 用户接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/comments | 发表评论 |
| GET | /api/v1/comments/my | 我的评论列表 |
| PUT | /api/v1/comments/{id} | 编辑自己的评论 |
| DELETE | /api/v1/comments/{id} | 删除自己的评论 |

#### 管理接口（需管理员权限）

| 方法 | 路径 | 说明 |
|------|------|------|
| CRUD | /api/v1/admin/articles | 文章管理 |
| PUT | /api/v1/admin/articles/{id}/publish | 发布文章 |
| PUT | /api/v1/admin/articles/{id}/recycle | 移入回收站 |
| PUT | /api/v1/admin/articles/{id}/restore | 恢复文章 |
| DELETE | /api/v1/admin/articles/{id} | 彻底删除 |
| GET | /api/v1/admin/articles/{id}/tags | 获取文章标签 |
| POST | /api/v1/admin/articles/{id}/tags | 绑定文章标签 |
| CRUD | /api/v1/admin/categories | 分类管理 |
| CRUD | /api/v1/admin/tags | 标签管理 |
| GET | /api/v1/admin/comments | 评论管理（全部） |
| PUT | /api/v1/admin/comments/{id}/approve | 审核通过 |
| PUT | /api/v1/admin/comments/{id}/reject | 审核驳回 |
| CRUD | /api/v1/admin/users | 用户管理 |
| GET | /api/v1/admin/configs | 获取所有配置 |
| PUT | /api/v1/admin/configs | 更新配置 |

---

## 7. 缓存策略 (Redis)

### 7.1 缓存场景

| 缓存 Key | 场景 | 过期时间 | 说明 |
|----------|------|----------|------|
| `article:list:{page}:{size}` | 文章列表 | 5 分钟 | 分页缓存 |
| `article:detail:{id}` | 文章详情 | 10 分钟 | 单篇文章缓存 |
| `category:list` | 分类列表 | 30 分钟 | 全量分类缓存 |
| `tag:list` | 标签列表 | 30 分钟 | 全量标签缓存 |
| `config:{key}` | 单个配置项 | 1 小时 | 配置缓存 |
| `user:token:{userId}` | 用户登录态 | 7 天 | JWT Token 存储 |
| `blacklist:token:{token}` | Token 黑名单 | 至 Token 过期 | 登出黑名单 |

### 7.2 缓存更新策略

- **主动更新**：数据变更时删除对应缓存
- **TTL 过期**：作为兜底保障
- **Cache Aside**：先读缓存，缓存未命中则查库并回填

---

## 8. 前端页面规划

### 8.1 前端展示端 (blog-web)

| 页面 | 路由 | 说明 |
|------|------|------|
| 首页 | `/` | 文章列表 + 分类侧边栏 + 标签云 |
| 文章详情 | `/article/:id` | 文章正文 + 评论区 |
| 分类页 | `/category/:slug` | 分类下的文章列表 |
| 标签页 | `/tag/:slug` | 标签下的文章列表 |
| 登录页 | `/login` | 用户登录 |
| 注册页 | `/register` | 用户注册 |

### 8.2 前端管理端 (blog-admin)

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 管理员登录 |
| 首页/仪表盘 | `/` | 统计数据概览 |
| 文章管理 | `/articles` | 文章列表 |
| 文章编辑 | `/articles/edit/:id?` | 新建/编辑文章 |
| 分类管理 | `/categories` | 分类 CRUD |
| 标签管理 | `/tags` | 标签 CRUD |
| 评论管理 | `/comments` | 评论审核 |
| 用户管理 | `/users` | 用户列表 |
| 系统配置 | `/settings` | 系统配置 |

### 8.3 小程序端 (blog-uniapp)

| 页面 | 说明 |
|------|------|
| 首页 | 文章列表（支持下拉刷新/上拉加载） |
| 文章详情 | 文章正文 + 评论区（仅查看） |
| 分类列表 | 分类筛选 |
| 关于页 | 网站信息 |

---

## 9. 项目目录结构

```
Xblog-mini/
│
├── doc/                           # 项目文档
│   ├── PRD.md                     # 本文档
│   └── api/                       # API 文档
│
├── blog-web/                      # 前端展示端
│   ├── src/
│   │   ├── api/                   # API 请求封装
│   │   ├── components/            # 公共组件
│   │   ├── pages/                 # 页面
│   │   ├── router/                # 路由
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── styles/                # 全局样式
│   │   └── utils/                 # 工具函数
│   ├── .env                       # 环境变量
│   └── vite.config.ts
│
├── blog-admin/                    # 前端管理端
│   ├── src/
│   │   ├── api/                   # API 请求封装
│   │   ├── components/            # 公共组件
│   │   ├── pages/                 # 页面
│   │   ├── router/                # 路由 + 权限控制
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── styles/                # 全局样式
│   │   └── utils/                 # 工具函数
│   ├── .env
│   └── vite.config.ts
│
├── blog-uniapp/                   # 小程序端
│   ├── src/
│   │   ├── api/                   # API 请求封装
│   │   ├── components/            # 公共组件
│   │   ├── pages/                 # 页面
│   │   ├── static/                # 静态资源
│   │   └── utils/                 # 工具函数
│   └── manifest.json
│
├── blog-api/                      # 后端服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/xblog/
│   │       │   ├── Main.java
│   │       │   ├── config/         # 配置类
│   │       │   ├── controller/    # 控制器
│   │       │   ├── service/       # 服务层
│   │       │   ├── mapper/        # 数据访问层
│   │       │   ├── entity/        # 实体类
│   │       │   ├── dto/           # 数据传输对象
│   │       │   ├── vo/            # 视图对象
│   │       │   ├── common/        # 公共类 (异常/响应/工具)
│   │       │   └── security/      # 安全相关 (JWT/权限)
│   │       └── resources/
│   │           ├── application.yml
│   │           └── mapper/         # MyBatis XML
│   └── pom.xml
│
├── docker/                        # Docker 相关
│   ├── nginx/
│   │   └── nginx.conf
│   └── redis/
│       └── redis.conf
│
├── sql/                           # SQL 脚本
│   ├── init.sql                   # 数据库初始化
│   └── test-data.sql              # 测试数据
│
├── docker-compose.yml             # 容器编排
└── README.md
```

---

## 10. 部署架构 (Docker Compose)

### 10.1 容器规划

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| nginx | nginx:alpine | 80/443 | 反向代理 + 静态资源 |
| blog-web | blog-web:latest | 3000 | 前端展示端 |
| blog-admin | blog-admin:latest | 3001 | 前端管理端 |
| blog-api | blog-api:latest | 8080 | 后端 API |
| mysql | mysql:8.0 | 3306 | 数据库 |
| redis | redis:7-alpine | 6379 | 缓存 |

### 10.2 Docker Compose 结构

```yaml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - blog-web
      - blog-admin
      - blog-api

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: xblog
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    command: redis-server /etc/redis/redis.conf
    volumes:
      - redis_data:/data
      - ./docker/redis/redis.conf:/etc/redis/redis.conf:ro
    ports:
      - "6379:6379"

  blog-api:
    build: ./blog-api
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_URL: jdbc:mysql://mysql:3306/xblog
      REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

  blog-web:
    build: ./blog-web
    ports:
      - "3000:3000"

  blog-admin:
    build: ./blog-admin
    ports:
      - "3001:3000"

volumes:
  mysql_data:
  redis_data:
```

---

## 11. 开发规范

### 11.1 Git 分支规范

| 分支 | 说明 |
|------|------|
| main | 主分支，生产环境 |
| develop | 开发分支 |
| feature/* | 功能分支 |
| fix/* | 修复分支 |

### 11.2 Git 提交规范

```
<type>(<scope>): <subject>

# type: feat | fix | docs | style | refactor | test | chore
# scope: 模块名，如 article, comment, auth
# subject: 简短描述
```

### 11.3 代码风格

- **后端**：遵循 Spring Boot 官方风格，Google Java Format
- **前端**：ESLint + Prettier，Vue 3 Composition API
- **命名**：语义化，驼峰/下划线按语言惯例

---

## 12. 非功能性需求

| 需求 | 说明 |
|------|------|
| **性能** | API 响应时间 < 200ms (P95)，首页加载 < 3s |
| **可用性** | 99.9% 正常运行时间 |
| **安全** | 密码 BCrypt 加密，SQL 注入防护，XSS 防护 |
| **兼容性** | Chrome/Firefox/Safari/Edge 最新两个版本，微信小程序 |

---

## 13. 版本计划

### v1.0.0 (MVP)

- [x] 需求确认
- [x] 数据库设计与实现
- [ ] 项目初始化
- [ ] 后端 API 开发
- [ ] 前端展示端开发
- [ ] 前端管理端开发
- [ ] 小程序端开发
- [ ] 部署与上线

---

*最后更新：2026-04-27*
