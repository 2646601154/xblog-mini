# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

轻量级个人博客系统，3模块架构：
- **blog-api**: Java 17 + Spring Boot 3.x 后端
- **blog-web**: Vue 3 + TypeScript 前台（Element Plus）
- **blog-admin**: Vue 3 + TypeScript 管理后台（Tailwind CSS v4）

## 常用命令

```bash
# 后端启动
cd blog-api
$env:DB_PASSWORD = "123456"
./mvnw spring-boot:run

# 前端启动
cd blog-web    # 或 blog-admin
pnpm install
pnpm dev

# 类型检查
pnpm type-check

# 构建
pnpm build

# Docker部署
cd docker
docker-compose up -d --build

# 数据库初始化
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.5, MyBatis-Plus 3.5, Redis, JWT |
| 前台/后台 | Vue 3.5, TypeScript 6, Vite 8, Pinia 3, Vue Router 5 |
| 样式 | blog-web: Element Plus; blog-admin: Tailwind CSS v4 |
| 数据库 | MySQL; API文档: Knife4j + SpringDoc OpenAPI |

## 后端架构

入口类：`com.xblog.Main`

```
blog-api/src/main/java/com/xblog/
├── common/          # 公共组件（Result, ResultCode, Util, Config）
├── controller/      # REST控制器（公开接口 + admin管理接口）
├── dto/             # 请求参数对象
├── entity/          # MyBatis-Plus实体类
├── handler/         # 处理器（Redis, MetaObject自动填充）
├── mapper/          # MyBatis Mapper接口
├── security/        # 安全组件（JWT, 拦截器）
├── service/         # 业务接口 + 实现
└── vo/              # 响应视图对象
```

### 后端开发规范

1. **接口路径**：Controller使用`/v1/`前缀，Nginx代理将`/api/v1/`重写为`/v1/`
2. **分层结构**：DTO → Service接口 → ServiceImpl → Controller
3. **分页安全**：`int pageNum = dto.getPage() != null ? dto.getPage() : 1`
4. **避免N+1**：批量查询用`selectBatchIds`收集关联数据
5. **公开接口**：在`common/config/WebMvcConfig.java`的`excludePathPatterns`中配置
6. **错误码**：在`common/enums/ResultCode.java`定义（认证1000-1999，用户2000-2999等）

### 已实现接口

**公开接口（12个）**：登录/注册、文章列表/详情/标签/评论、分类/标签/配置列表

**管理端接口（22个）**：文章/分类/标签/评论/用户/配置的完整CRUD

详细接口清单见 `blog-api/AGENTS.md`

## 前端架构

```
blog-{web|admin}/src/
├── api/           # Axios封装的API调用（modules/按模块分）
├── components/   # 复用组件
├── pages/         # 页面组件
├── router/        # Vue Router配置 + 路由守卫
├── stores/        # Pinia状态管理
└── utils/         # 工具函数
```

### 前端API调用

两套独立前端共享相同的API规范：
- `blog-web/src/api/` - 前台API
- `blog-admin/src/api/` - 管理后台API

使用Axios封装，路由前缀`/api/v1/`，Nginx代理到后端`/v1/`

## 数据库

- 初始化脚本：`sql/init.sql`
- 测试数据：`sql/test-data.sql`
- 逻辑删除：仅article表使用，其他表物理删除
- 自动填充：`MybatisPlusFillMetaObjectHandler`处理`createdAt`/`updatedAt`

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | admin |
| `testuser` | `123456` | user |

## 已知反模式（避免）

- ❌ 不要在`node_modules`目录放置项目文件
- ❌ 不要使用`@Autowired`，用`@Resource`
- ❌ 不要忘记分页null兜底
- ❌ 不要在循环内单独查询关联数据
- ❌ 不要修改配置后忘记清理Redis缓存
- ❌ 不要忘记清理ThreadLocal（`UserContext`使用ThreadLocal）
- ❌ `application.yml`中JWT secret是占位值，生产必须替换

## 关键文件位置

| 用途 | 路径 |
|------|------|
| 后端入口 | `blog-api/src/main/java/com/xblog/Main.java` |
| 业务错误码 | `blog-api/.../common/enums/ResultCode.java` |
| JWT配置 | `blog-api/.../security/JwtUtil.java` |
| Redis工具 | `blog-api/.../common/util/RedisUtil.java` |
| 前台文章详情 | `blog-web/src/pages/article/detail.vue` |
| 管理后台文章管理 | `blog-admin/src/pages/article/index.vue` |
| API文档 | `doc/api/`（auth.md, article.md等） |
| 前端API封装 | `blog-{web,admin}/src/api/request.ts` |

## 项目约束

- 后端无root聚合pom.xml，三模块独立构建
- 前端无项目级`.eslintrc`/`.prettierrc`
- 启动前**必须**设置`DB_PASSWORD`环境变量
- 数据库变更需同步`sql/init.sql`和`doc/PRD.md`
