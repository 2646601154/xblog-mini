# 评论模块 (Comment)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 4000 | 评论不存在 |
| 4004 | 不能编辑他人的评论 |
| 4005 | 不能删除他人的评论 |
| 4009 | 已审核通过的评论无法删除 |

---

## 获取文章评论列表 (公开)

### 请求

```
GET /api/v1/articles/{articleId}/comments
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| articleId | Long | Path | 是 | 文章 ID |
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "content": "写得很好！",
        "user": {
          "id": 2,
          "username": "testuser",
          "nickname": "测试用户",
          "avatar": "https://example.com/avatar.png"
        },
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

---

## 发表评论 (需登录)

### 请求

```
POST /api/v1/comments
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| articleId | Long | Body | 是 | 文章 ID |
| content | String | Body | 是 | 评论内容 (1-1000字符) |

### 请求示例

```json
{
  "articleId": 1,
  "content": "写得很好，期待后续更新！"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "articleId": 1,
    "content": "写得很好，期待后续更新！",
    "status": "pending",
    "createdAt": "2026-04-28T10:00:00"
  }
}
```

---

## 我的评论列表 (需登录)

### 请求

```
GET /api/v1/comments/my
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |
| status | String | Query | 否 | 状态筛选 (pending/approved/rejected) |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "article": {
          "id": 1,
          "title": "Spring Boot 3.x 快速入门指南"
        },
        "content": "写得很好！",
        "status": "approved",
        "createdAt": "2026-04-01T10:00:00",
        "updatedAt": "2026-04-01T10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "size": 10
  }
}
```

---

## 编辑评论 (需登录，仅自己的评论)

### 请求

```
PUT /api/v1/comments/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 评论 ID |
| content | String | Body | 是 | 评论内容 (1-1000字符) |

### 请求示例

```json
{
  "content": "更新后的评论内容"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "articleId": 1,
    "content": "更新后的评论内容",
    "status": "pending",
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-28T12:00:00"
  }
}
```

---

## 删除评论 (需登录，仅自己的评论)

### 业务说明

- 仅能删除自己的评论
- 已审核通过的评论无法删除（需先驳回再删除）

### 请求

```
DELETE /api/v1/comments/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 评论 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "articleId": 1,
    "content": "写得很好！",
    "status": "pending",
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

---

## 【管理】评论管理列表

### 请求

```
GET /api/v1/admin/comments
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |
| status | String | Query | 否 | 状态筛选 |
| articleId | Long | Query | 否 | 文章 ID 筛选 |
| userId | Long | Query | 否 | 用户 ID 筛选 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "article": {"id": 1, "title": "Spring Boot 3.x 快速入门指南"},
        "user": {"id": 2, "username": "testuser", "nickname": "测试用户"},
        "content": "写得很好！",
        "status": "pending",
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  }
}
```

---

## 【管理】审核通过评论

### 请求

```
PUT /api/v1/admin/comments/{id}/approve
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 评论 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "approved"
  }
}
```

---

## 【管理】驳回评论

### 请求

```
PUT /api/v1/admin/comments/{id}/reject
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 评论 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "rejected"
  }
}
```

---

## 【管理】删除评论

### 请求

```
DELETE /api/v1/admin/comments/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 评论 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```