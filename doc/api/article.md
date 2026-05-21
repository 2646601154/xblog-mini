# 文章模块 (Article)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 3000 | 文章不存在 |
| 3002 | 文章标题不能为空 |
| 3003 | 文章内容不能为空 |
| 3005 | 文章状态无效 |
| 3006 | 不能编辑他人的文章 |
| 3007 | 不能删除他人的文章 |
| 3009 | 文章已在回收站 |
| 3010 | 文章在回收站，无法发布 |
| 3011 | 文章不在回收站，无法恢复 |

---

## 文章列表

### 请求

```
GET /api/v1/articles
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |
| categoryId | Long | Query | 否 | 分类 ID 筛选 |
| tagId | Long | Query | 否 | 标签 ID 筛选 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Spring Boot 3.x 快速入门指南",
        "summary": "本文介绍了 Spring Boot 3.x 的基本概念...",
        "coverImage": "https://picsum.photos/seed/spring/800/400",
        "category": {
          "id": 1,
          "name": "技术",
          "slug": "tech"
        },
        "author": {
          "id": 1,
          "username": "admin",
          "nickname": "管理员",
          "avatar": "https://example.com/avatar.png"
        },
        "tags": [
          {"id": 1, "name": "Java", "slug": "java"},
          {"id": 2, "name": "Spring Boot", "slug": "spring-boot"}
        ],
        "viewCount": 100,
        "publishedAt": "2026-04-01T10:00:00",
        "createdAt": "2026-04-01T09:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

---

## 文章详情

### 请求

```
GET /api/v1/articles/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 3.x 快速入门指南",
    "summary": "本文介绍了 Spring Boot 3.x 的基本概念和快速搭建方法...",
    "content": "<h2>什么是 Spring Boot</h2><p>...</p>",
    "coverImage": "https://picsum.photos/seed/spring/800/400",
    "category": {
      "id": 1,
      "name": "技术",
      "slug": "tech"
    },
    "author": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://example.com/avatar.png"
    },
    "tags": [
      {"id": 1, "name": "Java", "slug": "java"},
      {"id": 2, "name": "Spring Boot", "slug": "spring-boot"}
    ],
    "status": "published",
    "viewCount": 101,
    "publishedAt": "2026-04-01T10:00:00",
    "createdAt": "2026-04-01T09:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

### 响应 (失败)

```json
{
  "code": 404,
  "message": "文章不存在",
  "data": null
}
```

---

## 获取文章上一篇/下一篇

### 请求

```
GET /api/v1/articles/{id}/prev-next
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "previous": {
      "id": 1,
      "title": "上一篇文章标题"
    },
    "next": {
      "id": 3,
      "title": "下一篇文章标题"
    }
  }
}
```

### 响应 (无上下篇)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "previous": null,
    "next": null
  }
}
```

---

## 获取文章标签

### 请求

```
GET /api/v1/articles/{id}/tags
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"id": 1, "name": "Java", "slug": "java"},
    {"id": 2, "name": "Spring Boot", "slug": "spring-boot"}
  ]
}
```

---

## 【管理】文章管理列表

### 请求

```
GET /api/v1/admin/articles
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |
| status | String | Query | 否 | 状态筛选 (draft/published/recycled) |
| categoryId | Long | Query | 否 | 分类 ID 筛选 |
| title | String | Query | 否 | 标题模糊搜索 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Spring Boot 3.x 快速入门指南",
        "summary": "本文介绍了...",
        "coverImage": "https://picsum.photos/seed/spring/800/400",
        "category": {"id": 1, "name": "技术", "slug": "tech"},
        "author": {"id": 1, "nickname": "管理员", "username": "admin", "avatar": "..."},
        "tags": [
          {"id": 1, "name": "Java", "slug": "java"},
          {"id": 2, "name": "Spring Boot", "slug": "spring-boot"}
        ],
        "status": "published",
        "viewCount": 100,
        "publishedAt": "2026-04-01T10:00:00",
        "createdAt": "2026-04-01T09:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

---

## 【管理】创建文章

### 请求

```
POST /api/v1/admin/articles
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| title | String | Body | 是 | 标题 (最多200字符) |
| summary | String | Body | 否 | 摘要 (最多500字符) |
| content | String | Body | 是 | 正文 (HTML) |
| coverImage | String | Body | 否 | 封面图 URL |
| categoryId | Long | Body | 是 | 分类 ID |
| tagIds | Long[] | Body | 否 | 标签 ID 数组 |
| status | String | Body | 否 | 状态 (draft/published)，默认 draft |

### 请求示例

```json
{
  "title": "Spring Boot 3.x 快速入门指南",
  "summary": "本文介绍了 Spring Boot 3.x 的基本概念和快速搭建方法",
  "content": "<h2>什么是 Spring Boot</h2><p>...</p>",
  "coverImage": "https://picsum.photos/seed/spring/800/400",
  "categoryId": 1,
  "tagIds": [1, 2],
  "status": "draft"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 3.x 快速入门指南",
    "status": "draft",
    "createdAt": "2026-04-28T10:00:00"
  }
}
```

---

## 【管理】更新文章

### 请求

```
PUT /api/v1/admin/articles/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |
| title | String | Body | 是 | 标题 |
| summary | String | Body | 否 | 摘要 |
| content | String | Body | 是 | 正文 |
| coverImage | String | Body | 否 | 封面图 |
| categoryId | Long | Body | 是 | 分类 ID |
| tagIds | Long[] | Body | 否 | 标签 ID 数组 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 3.x 快速入门指南 (更新)",
    "updatedAt": "2026-04-28T12:00:00"
  }
}
```

---

## 【管理】发布文章

### 请求

```
PUT /api/v1/admin/articles/{id}/publish
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "published",
    "publishedAt": "2026-04-28T12:00:00"
  }
}
```

---

## 【管理】移入回收站

### 请求

```
PUT /api/v1/admin/articles/{id}/recycle
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "recycled"
  }
}
```

---

## 【管理】恢复文章

### 请求

```
PUT /api/v1/admin/articles/{id}/restore
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "draft"
  }
}
```

---

## 【管理】彻底删除文章

### 请求

```
DELETE /api/v1/admin/articles/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 【管理】绑定文章标签

### 请求

```
POST /api/v1/admin/articles/{id}/tags
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 文章 ID |
| tagIds | Long[] | Body | 是 | 标签 ID 数组 |

### 请求示例

```json
{
  "tagIds": [1, 2, 3]
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"id": 1, "name": "Java", "slug": "java"},
    {"id": 2, "name": "Spring Boot", "slug": "spring-boot"},
    {"id": 3, "name": "Vue", "slug": "vue"}
  ]
}
```