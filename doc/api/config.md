# 系统配置模块 (Config)

## 业务错误码

| 错误码 | 说明 |
|--------|------|
| 9000 | 系统错误 |

---

## 获取公开配置 (公开)

### 请求

```
GET /api/v1/configs
```

### 请求参数

无

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "siteTitle": "Xblog - 我的技术博客",
    "siteLogo": "https://picsum.photos/seed/logo/200/60",
    "siteDescription": "一个分享技术、生活和感悟的个人博客",
    "icpNumber": "京ICP备XXXXXXXX号",
    "copyright": "© 2026 Xblog. All rights reserved."
  }
}
```

---

## 【管理】获取所有配置

### 请求

```
GET /api/v1/admin/configs
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
      "configKey": "site_title",
      "configValue": "Xblog - 我的技术博客",
      "description": "网站标题",
      "createdAt": "2026-04-01T09:00:00",
      "updatedAt": "2026-04-01T09:00:00"
    },
    {
      "id": 2,
      "configKey": "site_logo",
      "configValue": "https://picsum.photos/seed/logo/200/60",
      "description": "网站 Logo",
      "createdAt": "2026-04-01T09:00:00",
      "updatedAt": "2026-04-01T09:00:00"
    },
    {
      "id": 3,
      "configKey": "site_description",
      "configValue": "一个分享技术、生活和感悟的个人博客",
      "description": "网站描述",
      "createdAt": "2026-04-01T09:00:00",
      "updatedAt": "2026-04-01T09:00:00"
    },
    {
      "id": 4,
      "configKey": "icp_number",
      "configValue": "京ICP备XXXXXXXX号",
      "description": "备案号",
      "createdAt": "2026-04-01T09:00:00",
      "updatedAt": "2026-04-01T09:00:00"
    },
    {
      "id": 5,
      "configKey": "copyright",
      "configValue": "© 2026 Xblog. All rights reserved.",
      "description": "版权信息",
      "createdAt": "2026-04-01T09:00:00",
      "updatedAt": "2026-04-01T09:00:00"
    }
  ]
}
```

---

## 【管理】更新配置

### 请求

```
PUT /api/v1/admin/configs
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| configs | Config[] | Body | 是 | 配置数组 |

### 请求示例

```json
{
  "configs": [
    {
      "configKey": "site_title",
      "configValue": "Xblog - 新标题"
    },
    {
      "configKey": "site_description",
      "configValue": "更新后的网站描述"
    }
  ]
}
```

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "configKey": "site_title",
      "configValue": "Xblog - 新标题"
    },
    {
      "configKey": "site_description",
      "configValue": "更新后的网站描述"
    }
  ]
}
```

---

## 【管理】获取单个配置

### 请求

```
GET /api/v1/admin/configs/{key}
```

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| key | String | Path | 是 | 配置键 |

### 响应 (成功)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "configKey": "site_title",
    "configValue": "Xblog - 我的技术博客",
    "description": "网站标题",
    "createdAt": "2026-04-01T09:00:00",
    "updatedAt": "2026-04-01T09:00:00"
  }
}
```

---

## 配置项说明

| 配置键 | 说明 | 公开可见 |
|--------|------|----------|
| site_title | 网站标题 | 是 |
| site_logo | 网站 Logo | 是 |
| site_description | 网站描述 | 是 |
| icp_number | 备案号 | 是 |
| copyright | 版权信息 | 是 |
| admin_username | 管理员用户名 | 否 |
| admin_password | 管理员密码 (加密) | 否 |