# 用户管理模块 (User)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 2000 | 用户不存在 |
| 2001 | 用户名已存在 |
| 2002 | 用户已被禁用 |
| 2009 | 无法禁用管理员账号 |
| 2010 | 无法删除管理员账号 |

---

## 【管理】用户管理列表

### 请求

```
GET /api/v1/admin/users
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| page | Integer | Query | 否 | 页码 (默认 1) |
| size | Integer | Query | 否 | 每页条数 (默认 10) |
| role | String | Query | 否 | 角色筛选 (admin/user) |
| status | String | Query | 否 | 状态筛选 (normal/disabled) |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "avatar": "https://example.com/avatar.png",
        "email": "admin@example.com",
        "role": "admin",
        "status": "normal",
        "createdAt": "2026-04-01T09:00:00",
        "updatedAt": "2026-04-01T09:00:00"
      },
      {
        "id": 2,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "https://example.com/avatar.png",
        "email": "testuser@example.com",
        "role": "user",
        "status": "normal",
        "createdAt": "2026-04-02T10:00:00",
        "updatedAt": "2026-04-02T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

---

## 【管理】获取用户详情

### 请求

```
GET /api/v1/admin/users/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 用户 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "avatar": "https://example.com/avatar.png",
    "email": "admin@example.com",
    "role": "admin",
    "status": "normal",
    "createdAt": "2026-04-01T09:00:00",
    "updatedAt": "2026-04-01T09:00:00"
  }
}
```

---

## 【管理】更新用户

### 请求

```
PUT /api/v1/admin/users/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 用户 ID |
| nickname | String | Body | 是 | 昵称 (2-50字符) |
| email | String | Body | 否 | 邮箱 |
| avatar | String | Body | 否 | 头像 URL |
| role | String | Body | 否 | 角色 (admin/user) |
| status | String | Body | 否 | 状态 (normal/disabled) |

### 请求示例

```json
{
  "nickname": "管理员 (更新)",
  "email": "admin_new@example.com",
  "role": "admin",
  "status": "normal"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员 (更新)",
    "email": "admin_new@example.com",
    "role": "admin",
    "status": "normal",
    "updatedAt": "2026-04-28T12:00:00"
  }
}
```

---

## 【管理】禁用用户

### 请求

```
PUT /api/v1/admin/users/{id}/disable
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 用户 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "status": "disabled"
  }
}
```

### 响应 (失败)

```json
{
  "code": 2009,
  "message": "无法禁用管理员账号",
  "data": null
}
```

---

## 【管理】启用用户

### 请求

```
PUT /api/v1/admin/users/{id}/enable
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 用户 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "status": "normal"
  }
}
```

---

## 【管理】删除用户

### 请求

```
DELETE /api/v1/admin/users/{id}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | Long | Path | 是 | 用户 ID |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 响应 (失败)

```json
{
  "code": 2010,
  "message": "无法删除管理员账号",
  "data": null
}
```