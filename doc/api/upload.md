# 文件上传模块 (Upload)

## 业务错误码

无

---

## 【管理】上传文件

### 请求

```
POST /api/v1/admin/upload
```

### 请求格式

```
multipart/form-data
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| file | File | Body | 是 | 文件 (支持图片等) |
| dir  | String | Query | 否 | 存储目录，默认 `common`（如 `article`、`avatar`） |


### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss.example.com/article/2026/05/26/xxx.jpg"
}
```