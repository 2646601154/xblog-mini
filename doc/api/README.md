# Xblog-mini 接口文档

## 文档目录

| 文档 | 说明 |
|------|------|
| [auth.md](./auth.md) | 认证相关 (登录/注册) |
| [article.md](./article.md) | 文章相关 (含标签) |
| [comment.md](./comment.md) | 评论相关 |
| [category.md](./category.md) | 分类相关 |
| [tag.md](./tag.md) | 标签相关 |
| [user.md](./user.md) | 用户管理 (Admin) |
| [config.md](./config.md) | 系统配置 |
| [upload.md](./upload.md) | 文件上传 |

---

## 通用说明

### 基础路径
```
/api/v1
```

### 认证方式
- Bearer Token (JWT)
- Header: `Authorization: Bearer <token>`

### 请求格式
- Content-Type: `application/json`

---

## 响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 分页响应
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

### 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": {
    "field": "错误信息"
  }
}
```

---

## HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 请求无效 |
| 401 | 未认证 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 业务错误码

| 范围 | 模块 |
|------|------|
| 1000-1999 | 认证模块 |
| 2000-2999 | 用户模块 |
| 3000-3999 | 文章模块 |
| 4000-4999 | 评论模块 |
| 5000-5999 | 分类模块 |
| 6000-6999 | 标签模块 |
| 9000-9999 | 系统级错误 |