# 分类模块 (Category)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 5000 | 未知错误 |
| 5001 | 分类不存在 |
| 5002 | 分类名称不能为空 |
| 5003 | 分类名称已存在 |
| 5004 | 分类 slug 已存在 |
| 5005 | 分类名称格式错误 (2-20字符) |
| 5006 | 无法删除有文章的分类 |
| 5007 | 分类排序值无效 |

---

## 获取分类列表 (公开)

### 请求

```
GET /api/v1/categories
```

### 请求参数

无

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"id": 1, "name": "技术", "slug": "tech", "description": "技术分享与教程", "sortOrder": 1},
    {"id": 2, "name": "生活", "slug": "life", "description": "生活感悟与随笔", "sortOrder": 2}
  ]
}
```

---

## 【管理】分类管理列表

### 请求

```
GET /api/v1/admin/categories
```

### 请求参数

无

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "技术",
      "slug": "tech",
      "description": "技术分享与教程",
      "sortOrder": 1,
      "articleCount": 10,
      "createdAt": "2026-04-01T09:00:00"
    }
  ]
}
```

---

## 【管理】创建分类

### 请求

```
POST /api/v1/admin/categories
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| name | String | Body | 是 | 分类名称 (2-20字符) |
| slug | String | Body | 是 | URL 标识 (1-50字符) |
| description | String | Body | 否 | 分类描述 (最多200字符) |
| sortOrder | Integer | Body | 否 | 排序值 (默认0，越小越靠前) |

### 请求示例

```json
{
  "name": "技术",
  "slug": "tech",
  "description": "技术分享与教程",
  "sortOrder": 1
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "技术",
    "slug": "tech",
    "description": "技术分享与教程",
    "sortOrder": 1
  }
}
```

---

## 【管理】更新分类

### 请求

```
PUT /api/v1/admin/categories/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 分类 ID |
| name | String | Body | 是 | 分类名称 |
| slug | String | Body | 是 | URL 标识 |
| description | String | Body | 否 | 分类描述 |
| sortOrder | Integer | Body | 否 | 排序值 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "技术",
    "slug": "tech",
    "description": "技术分享与教程 (更新)",
    "sortOrder": 2
  }
}
```

---

## 【管理】删除分类

### 请求

```
DELETE /api/v1/admin/categories/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 分类 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 响应 (失败 - 有文章关联)

```json
{
  "code": 400,
  "message": "无法删除有文章的分类",
  "data": null
}
```