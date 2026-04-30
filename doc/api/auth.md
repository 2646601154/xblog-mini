# 认证模块 (Auth)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1000 | 登录失败（用户名或密码错误） |
| 1001 | Token 已过期 |
| 1002 | Token 无效 |
| 2001 | 用户名已存在 |
| 2002 | 用户已被禁用 |
| 2003 | 邮箱已存在 |

---

## Token 说明

- **算法**：HS256 (HMAC-SHA256)
- **有效期**：7 天
- **密钥来源**：`jwt.secret` 配置项
- **Claims 结构**：

| Claim | 类型 | 说明 |
|-------|------|------|
| `sub` | String | 用户名 |
| `userId` | Long | 用户 ID |
| `role` | String | 角色（admin/user） |
| `iat` | Date | 签发时间 |
| `exp` | Date | 过期时间 |

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

### 响应 (失败 - 用户名或密码错误)

```json
{
  "code": 1000,
  "message": "用户名或密码错误",
  "data": null
}
```

### 响应 (失败 - 用户已禁用)

```json
{
  "code": 2002,
  "message": "用户已被禁用",
  "data": null
}
```

### 响应 (失败 - 参数校验)

```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": {
    "username": "用户名不能为空"
  }
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

### 响应 (失败 - 用户名已存在)

```json
{
  "code": 2001,
  "message": "用户名已存在",
  "data": null
}
```

### 响应 (失败 - 邮箱已存在)

```json
{
  "code": 2003,
  "message": "邮箱已存在",
  "data": null
}
```

### 响应 (失败 - 参数校验)

```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": {
    "username": "用户名需3-20位，字母开头",
    "password": "密码最少6位",
    "nickname": "昵称不能为空"
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

### 响应 (失败 - Token 无效/过期)

```json
{
  "code": 1002,
  "message": "Token 无效",
  "data": null
}
```