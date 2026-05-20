# Xblog-mini 微信小程序

个人博客微信小程序，连接现有 `blog-api` 后端服务。

## 功能特性

- [x] 首页文章列表（分页加载）
- [x] 文章详情阅读
- [x] 分类/标签筛选
- [x] 用户登录/注册
- [x] 评论互动
- [x] 微信分享（好友/朋友圈）
- [x] 搜索功能
- [x] 用户中心

## 项目结构

```
blog-mobile/
├── app.js              # App 入口
├── app.json            # 全局配置
├── app.wxss            # 全局样式
├── pages/              # 页面目录
│   ├── index/          # 首页 - 文章列表
│   ├── article/        # 文章详情
│   ├── category/       # 分类页
│   ├── search/         # 搜索页
│   └── user/           # 用户中心、登录、注册
├── components/         # 公共组件
├── services/           # API 服务层
├── utils/              # 工具函数
└── static/             # 静态资源
```

## 快速开始

### 1. 配置后端地址

修改 `app.js` 中的 `globalData.baseUrl`:

```javascript
globalData: {
  baseUrl: 'https://your-api-domain.com/api/v1'
}
```

### 2. 配置域名白名单

在微信公众平台 → 开发管理 → 开发设置 中添加：
- 允许请求的域名（request 合法域名）
- 允许uploadFile的域名
- 允许downloadFile的域名

### 3. 配置 AppID

修改 `project.config.json` 中的 `appid` 为你的小程序 AppID。

### 4. 安装依赖

```bash
# 使用微信开发者工具打开项目
# 工具会自动安装依赖
```

## API 对接

直接复用 `blog-api` 后端接口：

| 接口 | 说明 |
|------|------|
| `GET /articles` | 文章列表 |
| `GET /articles/{id}` | 文章详情 |
| `GET /articles/{id}/comments` | 文章评论 |
| `POST /comments` | 发表评论 |
| `POST /auth/login` | 用户登录 |
| `POST /auth/register` | 用户注册 |
| `GET /categories` | 分类列表 |
| `GET /tags` | 标签列表 |

## 微信登录（可选）

如需微信一键登录，需后端新增接口：

```javascript
// POST /auth/wechat-login
// 请求体
{
  "code": "微信登录code",
  "nickname": "用户昵称",
  "avatar": "头像URL"
}
```

## 开发说明

- 使用原生框架开发，性能最优
- 样式使用 CSS 变量，便于主题定制
- 组件化开发，方便维护
- 支持分包加载，优化首屏速度

## 许可证

MIT
