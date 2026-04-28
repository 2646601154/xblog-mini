# 标签模块 (Tag)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 6000 | 未知错误 |
| 6001 | 标签不存在 |
| 6002 | 标签名称不能为空 |
| 6003 | 标签名称已存在 |
| 6004 | 标签 slug 已存在 |
| 6005 | 标签名称格式错误 (1-20字符) |
| 6006 | 标签 slug 格式错误 (1-50字符) |

---

## 获取标签列表 (公开)

### 请求

```
GET /api/v1/tags
```

### 请求参数

无

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

---

## 【管理】标签管理列表

### 请求

```
GET /api/v1/admin/tags
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
      "name": "Java",
      "slug": "java",
      "articleCount": 5,
      "createdAt": "2026-04-01T09:00:00"
    }
  ]
}
```

---

## 【管理】创建标签

### 请求

```
POST /api/v1/admin/tags
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| name | String | Body | 是 | 标签名称 (1-20字符) |
| slug | String | Body | 是 | URL 标识 (1-50字符) |

### 请求示例

```json
{
  "name": "Java",
  "slug": "java"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Java",
    "slug": "java"
  }
}
```

---

## 【管理】更新标签

### 请求

```
PUT /api/v1/admin/tags/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 标签 ID |
| name | String | Body | 是 | 标签名称 |
| slug | String | Body | 是 | URL 标识 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Java",
    "slug": "java"
  }
}
```

---

## 【管理】删除标签

### 请求

```
DELETE /api/v1/admin/tags/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 标签 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```