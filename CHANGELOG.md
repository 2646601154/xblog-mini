# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## [0.2.0](https://github.com/jnMetaCode/xblog-mini/compare/v0.1.0...v0.2.0) (2026-07-09)


### ✅ 测试

* **common:** 添加OssUtil单元测试 ([9ee6fcd](https://github.com/jnMetaCode/xblog-mini/commits/9ee6fcddffedf5f00ff99e2de6d1e4c9e81ef7e3))
* DashboardServiceImpl 单元测试（10个用例） ([6f2207f](https://github.com/jnMetaCode/xblog-mini/commits/6f2207f2f0860443fa0b4684eb7011fd6818b170))


### ✨ 新功能

* 启动成功打印Xblog图形、新增IpUtil和缓存常量、添加本地开发配置 ([f22fa77](https://github.com/jnMetaCode/xblog-mini/commits/f22fa77c820563a61581c3655ec50d27dad3530f))
* 启动成功打印Xblog图形、新增IpUtil和缓存常量、优化application.yml ([54b565b](https://github.com/jnMetaCode/xblog-mini/commits/54b565b582a63224bc6ee4f86c84ece38d37ad62))
* 小程序初始化 ([82a9a6e](https://github.com/jnMetaCode/xblog-mini/commits/82a9a6e848a53c360c40c9132cbfd9faf954bbc2))
* 新增普通用户修改资料和修改密码接口 ([a5d615e](https://github.com/jnMetaCode/xblog-mini/commits/a5d615edfcb8ed36c250c6d04b3c1d97ba2afc17))
* 仪表盘统计接口 + OSS 目录隔离 ([e7ec6a1](https://github.com/jnMetaCode/xblog-mini/commits/e7ec6a1b6b90f468a46f9b1e3d275381766efc1e))
* add dashboard API endpoint and replace mock data ([f923237](https://github.com/jnMetaCode/xblog-mini/commits/f9232372d9a9fcc0d3ed4fae3b6c57ed4f0c934b))
* admin 端新增图片管理功能 ([05e2e78](https://github.com/jnMetaCode/xblog-mini/commits/05e2e78e4aa61fb42c6ede35f003f22506276f73))
* **admin:** 401弹窗提示、草稿删除与API路径调整 ([7e18ba7](https://github.com/jnMetaCode/xblog-mini/commits/7e18ba74142d5e61a5e58fad73c6790398b7edfb))
* **admin:** 管理后台增加管理员角色校验 ([ced457a](https://github.com/jnMetaCode/xblog-mini/commits/ced457a6365dd899fe96fb6aa573314afe0a1313))
* admin添加重置密码功能 ([a382e81](https://github.com/jnMetaCode/xblog-mini/commits/a382e815927658e3560128d742ea478f2cf34fc1))
* **admin:** 优化分类、评论和标签管理界面UI ([82d2972](https://github.com/jnMetaCode/xblog-mini/commits/82d2972119efc0311b6fbbee8ff17f5b75bc3c92))
* **archive:** 文章归档页面按发布日期排序 ([9af53a2](https://github.com/jnMetaCode/xblog-mini/commits/9af53a2f4ab5cff7b68aa82b3ac20da796406475))
* **article:** 添加文章上一篇下一篇功能和管理后台图表 ([e29dfcc](https://github.com/jnMetaCode/xblog-mini/commits/e29dfcc653621bf3fbbcafa6f0feebe1a071ba2d))
* **article:** 为文章服务添加Redis缓存支持 ([6370489](https://github.com/jnMetaCode/xblog-mini/commits/6370489258351685b5fb94f3f9d7e3b800032ec1))
* **config:** 添加文章上下篇接口路径配置 ([510532d](https://github.com/jnMetaCode/xblog-mini/commits/510532d35582129decbeb87b27fe15b15d422bf3))
* **profile:** 个人中心页面重写及目录迁移 ([bd3f015](https://github.com/jnMetaCode/xblog-mini/commits/bd3f0156b3625366733eb597694f7341f6b17384))
* **sidebar:** 更新博主名称为Xiaruoxin ([c4bcd85](https://github.com/jnMetaCode/xblog-mini/commits/c4bcd85df1a56a88adcd849eb1e78f10b5f1489b))
* **theme:** 添加深色模式支持 ([0b4272e](https://github.com/jnMetaCode/xblog-mini/commits/0b4272e2e4bef76e923966fbbd26b19df00cc9b8))


### 🐛 修复

* 更换封面图/头像时自动删除 OSS 上的旧图片 ([0740731](https://github.com/jnMetaCode/xblog-mini/commits/07407313366e886a8c0e22e8da3f32e1c8bc7b23))
* 实现头像上传和文章封面图上传功能 ([67a3d9b](https://github.com/jnMetaCode/xblog-mini/commits/67a3d9bbea55cec93280646aa8502fd9d05eab77)), closes [#1](https://github.com/2646601154/xblog-mini/issues/1) [#5](https://github.com/2646601154/xblog-mini/issues/5)
* **图片管理:** 修复 OSS URL 前缀为空时所有图片被误判为 OSS 来源的问题 ([4ab0b13](https://github.com/jnMetaCode/xblog-mini/commits/4ab0b13c2f808977d95caea03485fa7161dc319b))
* 修复 public 资源路径，添加社交媒体链接 ([c19fd56](https://github.com/jnMetaCode/xblog-mini/commits/c19fd566ed4f29e2d626c61b27301fb2bbd09589))
* 修复 web 端登录认证与 401 跳转问题 ([80da621](https://github.com/jnMetaCode/xblog-mini/commits/80da621b20fa827f5a8ffe5881e188aaf7f8a581))
* **admin:** 修复文章编辑、刷新登出与表单校验问题 ([a3fe80f](https://github.com/jnMetaCode/xblog-mini/commits/a3fe80fb30e91c80d6741040c62b65da0452b5fc))
* **auth:** 退出登录修复 ([ec12028](https://github.com/jnMetaCode/xblog-mini/commits/ec12028b5144797319b15b898f08addf208da428))
* **blog-api:** 修复 updateUser 暴露 User 实体 ([1e5e4f7](https://github.com/jnMetaCode/xblog-mini/commits/1e5e4f7d89669d950eb393ccff9ee78273fcf19a))
* **layout:** 登录/注册页使用全屏布局（无 Header/Footer） ([531a45f](https://github.com/jnMetaCode/xblog-mini/commits/531a45f517445b5f6ce125452bdf32a812e15aeb))
* **mobile:** 文章详情页未登录可浏览，评论需登录后发表 ([b9b11ed](https://github.com/jnMetaCode/xblog-mini/commits/b9b11ed54118285dbe93f707814ac50db9ac6518))
* **OSS:** 修复本地未配置 OSS 时项目无法启动的问题 ([0e7afba](https://github.com/jnMetaCode/xblog-mini/commits/0e7afba90f70c30a60bf8b147b0d5896016c5256))
* **styles:** 调整背景色以改善视觉效果 ([9b455f8](https://github.com/jnMetaCode/xblog-mini/commits/9b455f825de725b854cb6f68c9ecb51ed90d6b04))
* UserController自定义Bean名为adminUserController ([38bd3e7](https://github.com/jnMetaCode/xblog-mini/commits/38bd3e771ce2c452c2b81d46e4084a1d9cb76eda))
* **web:** restore app header component ([4e7ef5b](https://github.com/jnMetaCode/xblog-mini/commits/4e7ef5b30ddf8cca9c9eff7966f07004c5898f78))


### ♻️ 重构

* 精简系统配置，移除 title/logo/description，新增 admin_username/password ([0081649](https://github.com/jnMetaCode/xblog-mini/commits/0081649ccc693dc641e3e1c9c6d8d2a81a580367))
* 统一 API 模块返回类型别名，提取公共 NullResponse ([b0274e7](https://github.com/jnMetaCode/xblog-mini/commits/b0274e7e63a4c114ff44799004f3b784d99db5ad))
* 统一代码风格，修正类型定义和接口参数 ([a788ea6](https://github.com/jnMetaCode/xblog-mini/commits/a788ea620fcd3817ff4a33d3cb26366184be84e9))
* 统一接口名称和类型定义，提升代码一致性和可读性 ([7f61e9f](https://github.com/jnMetaCode/xblog-mini/commits/7f61e9fcd3e62144e149a86f2c1e9e877f9c37b6))
* **admin:** 统一管理后台各页面 UI 样式 ([a2b45a0](https://github.com/jnMetaCode/xblog-mini/commits/a2b45a04865410731fac101c6c00c0b24877ad3d))
* **article:** 重构文章服务依赖注入和浏览量统计逻辑 ([6e5dc98](https://github.com/jnMetaCode/xblog-mini/commits/6e5dc98c0e9f885c2b877ef0d6d4dbeb1121c03f))
* **blog-api:** 业务错误码文案脱敏 ([c37b7be](https://github.com/jnMetaCode/xblog-mini/commits/c37b7bed5e882cb92461c4140e93e23dcc88d6a4))
* **blog-api:** 用户响应字段白名单（VO 化） ([deb5915](https://github.com/jnMetaCode/xblog-mini/commits/deb59155f7ea1ab3f8399c5452707cce2291e050))
* **blog-api:** Renamed shadowed page variable and cleaned up code formatting ([48d965a](https://github.com/jnMetaCode/xblog-mini/commits/48d965a44baa08aaee37642a6c00e6cf5efcf97a))
* normalize JWT interceptor package ([756b57b](https://github.com/jnMetaCode/xblog-mini/commits/756b57bbc6931d4b61ec948058e58e126976ab2d))
* Redis 缓存优化与 SCAN 替代 KEYS 命令 ([2b34668](https://github.com/jnMetaCode/xblog-mini/commits/2b346684f0b587cdeeed7ef10b04b2cd7a09d48a))
* remove admin username/password from config page and seed data ([3626743](https://github.com/jnMetaCode/xblog-mini/commits/3626743dad072dd41aaf6cb669f897e27357f810))
* **service:** 使用PageUtil统一处理分页逻辑并优化RedisUtil ([2282d13](https://github.com/jnMetaCode/xblog-mini/commits/2282d1359327ba10bfe36ae5ef8047157c059e90))
* update styles and improve layout across multiple views ([45fbead](https://github.com/jnMetaCode/xblog-mini/commits/45fbead3ede5f24960bd30593194778a969b4bf0))


### 📝 文档

* 同步 PRD 文档与项目实际实现，补充额外功能并修正选型 ([c8a22db](https://github.com/jnMetaCode/xblog-mini/commits/c8a22db46f236b7b7da2a3b3632c4f1ff9500d26))
* **AGENTS.md:** 更新项目文档移除过时内容并优化反模式说明 ([ddcc413](https://github.com/jnMetaCode/xblog-mini/commits/ddcc4131515f2faa25aeaa0f68915d92b0c0da70))
* **AGENTS:** 刷新项目知识库，同步最新架构和约定 ([bd7e80d](https://github.com/jnMetaCode/xblog-mini/commits/bd7e80de43447e5990e654f4ec3bb9e97454e0e8))
* **blog-api:** Simplified and updated AGENTS.md development guidelines ([f6b0620](https://github.com/jnMetaCode/xblog-mini/commits/f6b0620c73d13c10e1d39268ab9d79d43ffc549e))
* **project:** 更新项目文档，优化描述信息 ([714e308](https://github.com/jnMetaCode/xblog-mini/commits/714e3089b962e744b46cac75dcabb893e437ac78))
* **project:** 更新项目文档，优化描述信息 ([4f6eced](https://github.com/jnMetaCode/xblog-mini/commits/4f6eced6cd0d50b8eb87a7a1cd354c2c45846766))
* **project:** 更新项目知识库文档 ([bbe07f0](https://github.com/jnMetaCode/xblog-mini/commits/bbe07f0de692165aff661ab2f806a35d55bd089a))


### 🎨 样式调整

* **样式:** 统一 web 端圆角阴影并向 admin 端对齐，删除所有渐变 ([32dffe6](https://github.com/jnMetaCode/xblog-mini/commits/32dffe6ee14b6f01e31051cf700a0d527b111a98))
* **AppHeader:** 移除登录按钮的small尺寸样式 ([c684e26](https://github.com/jnMetaCode/xblog-mini/commits/c684e262a443c73c079b85b2abb7527fadd5cda8))


### 🔧 杂项

* 安装 ESLint 和 Prettier 依赖，格式化代码 ([aedaace](https://github.com/jnMetaCode/xblog-mini/commits/aedaace71bc3ce2e74281d85f1cb5473bcce8561))
* **构建:** 将所有 .gitignore 统一到根目录 ([3a10d3c](https://github.com/jnMetaCode/xblog-mini/commits/3a10d3cb989e201c2be100db6311705b37a4b349))
* 将 .omo/ 目录加入 .gitignore ([31141e3](https://github.com/jnMetaCode/xblog-mini/commits/31141e3cd59abd87977d94c50a3a66a5f01d1903))
* 配置 ESLint 和 Prettier，格式化代码 ([f9f859f](https://github.com/jnMetaCode/xblog-mini/commits/f9f859f7dac15596070762d2d5453589b8c66043))
* 清理不必要的文件并格式化代码 ([c833a1e](https://github.com/jnMetaCode/xblog-mini/commits/c833a1e40728869a0e040c06b5220bd7ccdffded))
* 清理无用文件并补充 env.d.ts ([7f3d072](https://github.com/jnMetaCode/xblog-mini/commits/7f3d072e87d0d5379f44970e352dc2cb92dbacca))
* 取消 application-local.yml 的 Git 追踪 ([5df4d5d](https://github.com/jnMetaCode/xblog-mini/commits/5df4d5d0143b6aab0003dc072ced7e1adbc6775e))
* 添加 .gitignore 并取消跟踪 .reasonix/ 目录 ([facea0e](https://github.com/jnMetaCode/xblog-mini/commits/facea0e9db867c524ea63a428baaad5c2c568e33))
* **blog-api:** 清理未使用的枚举类和错误码 ([8c8e5a4](https://github.com/jnMetaCode/xblog-mini/commits/8c8e5a4e2fd1c7bbb4dcd92d3e009fa769854c71))
* remove dead code and unused dependencies ([ec1ed38](https://github.com/jnMetaCode/xblog-mini/commits/ec1ed388ad2502eef519f87a571e67377850db60))
* update .gitignore for cross-platform portability ([46b1bab](https://github.com/jnMetaCode/xblog-mini/commits/46b1babdfb50536ba9bfdfabb6019ce3524a011b))
