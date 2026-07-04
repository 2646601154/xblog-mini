# docker 部署指南

**Generated:** 2026-05-20
**Commit:** b9b11ed
**父级**: `../AGENTS.md`

## OVERVIEW

Docker 7服务编排（Nginx反向代理 + Spring Boot + MySQL 8 + Redis 7 + SSL自动续期）。

## STRUCTURE

```
docker/
├── docker-compose.yml      # 编排定义（7个服务）
├── .env / .env.example     # 环境变量配置
├── Dockerfile.api          # Spring Boot后端（JDK 17 JRE，非root用户）
├── Dockerfile.web          # blog-web前台（Nginx静态服务）
├── Dockerfile.admin        # blog-admin后台（Nginx静态服务）
├── Dockerfile.mysql        # MySQL 8（含初始化脚本挂载）
├── Dockerfile.redis        # Redis 7（动态密码设置）
├── my.cnf                  # MySQL配置（utf8mb4 + 性能优化）
├── redis.conf              # Redis 7配置
├── nginx/                  # 站点配置
│   ├── web.conf            # blog-web站点
│   └── admin.conf          # blog-admin站点
├── nginx-proxy/            # 反向代理（SSL termination + 子域名路由）
│   ├── conf.d/
│   │   ├── default.conf    # 反向代理规则
│   │   └── options-ssl-nginx.conf
│   └── startup.sh
├── rainyun_certbot.py      # 雨云DNS API泛域名证书自动续期（非标准certbot）
├── sql/                    # 数据库初始化
│   ├── init.sql
│   └── test-data.sql
├── dist-web/               # 前台构建产物（不应提交）
├── dist-admin/             # 后台构建产物（不应提交）
└── blog-api-0.0.1-SNAPSHOT.jar  # 后端构建产物（不应提交）
```

## WHERE TO LOOK

| 任务 | 路径 |
|------|------|
| 首次部署指南 | `DEPLOY.md` |
| 服务编排 | `docker-compose.yml` |
| Nginx 反向代理规则 | `nginx-proxy/conf.d/default.conf` |
| 站点配置 | `nginx/web.conf`、`nginx/admin.conf` |
| 环境变量 | `.env` |
| 数据库初始化 | `sql/init.sql` |

## CONVENTIONS

- `docker-compose.yml` 定义7个服务：`nginx-proxy`、`certbot`、`blog-mysql`、`blog-redis`、`blog-api`、`blog-admin`、`blog-web`
- Nginx反向代理将 `/api/v1/*` 重写为 `/v1/*` 后转发给 `blog-api`
- SSL证书使用自定义Python脚本 `rainyun_certbot.py` + 雨云DNS API（DNS-01验证，非标准certbot）
- 静态资源由 `nginx-proxy` 直接服务，`blog-web`/`blog-admin` 容器仅提供兜底
- `.env` 中必须设置 `DB_PASSWORD`、`MYSQL_ROOT_PASSWORD`
- `JWT_SECRET` 在 `docker-compose.yml` 中有默认值 `xblog-jwt-secret-key-change-in-production-2024`，生产必须通过 `.env` 覆盖
- 一键构建：根目录 `build.sh`（编译后端+构建双前端→复制到docker目录）

## ANTI-PATTERNS

- ❌ **不要**将 `dist-web/`、`dist-admin/`、`*.jar` 提交到Git（构建产物）
- ❌ **不要**在 `.env` 中使用弱密码或默认密码
- ❌ **不要**直接修改 `nginx-proxy/conf.d/default.conf` 而不测试Nginx配置语法
- ❌ **不要**使用 `docker-compose.yml` 中的JWT_SECRET默认值（生产环境必须覆盖）

## COMMANDS

```bash
# 启动全部服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f blog-api

# 重启 Nginx 代理
docker-compose restart nginx-proxy

# 数据库初始化
mysql -h 127.0.0.1 -u root -p < sql/init.sql
```
