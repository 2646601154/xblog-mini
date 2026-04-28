# 认证模块 (Auth)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1000 | 未知错误 |
| 1001 | 用户名或密码错误 |
| 1002 | Token 已过期 |
| 1003 | Token 无效 |
| 1004 | 未登录访问需认证的资源 |
| 1005 | 无权限访问 |
| 1006 | 用户已禁用 |
| 1007 | 用户名已存在 |
| 1008 | 邮箱已存在 |
| 1009 | 注册失败 |
| 1010 | 参数校验失败 |

---

## 登录

### 请求

```
POST /api/v1/auth/login
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| username | String | Body | 是 | 用户名 |
| password | String | Body | 是 | 密码 (6-20位) |

### 请求示例

```json
{
  "username": "admin",
  "password": "123456"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://example.com/avatar.png",
      "role": "admin"
    }
  }
}
```

### 响应 (失败)

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 注册

### 请求

```
POST /api/v1/auth/register
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| username | String | Body | 是 | 用户名 (3-20字符，字母开头) |
| password | String | Body | 是 | 密码 (最少6字符) |
| nickname | String | Body | 是 | 昵称 (2-50字符) |
| email | String | Body | 否 | 邮箱 |

### 请求示例

```json
{
  "username": "newuser",
  "password": "123456",
  "nickname": "新用户",
  "email": "newuser@example.com"
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "username": "newuser",
    "nickname": "新用户",
    "email": "newuser@example.com",
    "role": "user"
  }
}
```

### 响应 (失败)

```json
{
  "code": 400,
  "message": "用户名已存在",
  "errors": {
    "username": "用户名已存在"
  }
}
```

---

## 获取当前用户信息

### 请求

```
GET /api/v1/auth/me
```

### 请求参数

无

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
    "status": "normal"
  }
}
```

### 响应 (失败)

```json
{
  "code": 401,
  "message": "Token 已过期",
  "data": null
}
```