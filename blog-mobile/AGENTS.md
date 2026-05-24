# blog-mobile 微信小程序开发指南

**Generated:** 2026-05-20
**Commit:** b9b11ed
**父级**: `../AGENTS.md`

## OVERVIEW

ir|微信小程序，连接blog-api后端。非Vue架构，使用原生App.js入口。

## STRUCTURE

```
blog-mobile/
├── app.js              # 入口（globalData.baseUrl配置后端地址）
├── app.json            # 全局配置（页面路由、tabBar、window样式）
├── app.wxss            # 全局样式
├── pages/              # 页面目录
│   ├── index/          # 首页 - 文章列表（分页）
│   ├── article/        # 文章详情
│   ├── category/       # 分类页
│   ├── search/         # 搜索页（标签筛选）
│   └── user/           # 用户中心、登录、注册
├── services/           # API服务层（封装wx.request）
├── utils/              # 工具函数
├── static/icons/       # tabBar图标（home/category/search/user）
├── __tests__/          # Jest测试（jest-miniprogram preset）
└── __mocks__/          # 测试mock
```

## WHERE TO LOOK

| 任务 | 路径 |
|------|------|
| 后端地址配置 | `app.js` → `globalData.baseUrl` |
| API服务层 | `services/` 目录 |
| tabBar配置 | `app.json` → `tabBar` |
| 首页逻辑 | `pages/index/index.js` |
| 微信登录 | 需后端新增 `/auth/wechat-login` 接口 |

## CONVENTIONS

### API调用
- 使用原生 `wx.request`，非axios
- 后端地址在 `app.js` 的 `globalData.baseUrl` 配置
- 直接复用blog-api接口（`/v1/articles`、`/v1/comments` 等）

### 页面路由
- 7个页面：index、article、category、search、user、user/login、user/register
- tabBar 4个tab：首页、分类、标签（search页）、我的
- 启用下拉刷新：`enablePullDownRefresh: true`
- 分包懒加载：`lazyCodeLoading: "requiredComponents"`

### 样式
- 原生CSS + CSS变量（主题定制）
- 全局样式在 `app.wxss`
- 背景色：`#f8f8f8`，导航栏：白色

## ANTI-PATTERNS

- ❌ **不要**在 `app.js` 中硬编码后端URL，使用 `globalData.baseUrl`
- ❌ **不要**绕过 `services/` 层直接调用 `wx.request`
- ❌ **不要**忘记在微信公众平台配置请求域名白名单
- ❌ **不要**将 `project.private.config.json` 提交到Git（含个人配置）

## COMMANDS

```bash
# 使用微信开发者工具打开项目
# 工具自动安装依赖并编译

# 测试（Jest）
cd blog-mobile
npm test
```

## NOTES

- 原生小程序框架，非Vue/React
- 需要在微信公众平台配置：request合法域名、uploadFile域名、downloadFile域名
- `project.config.json` 中 `appid` 需改为实际小程序AppID
- 微信登录需后端新增接口：`POST /auth/wechat-login`（接收微信code）
