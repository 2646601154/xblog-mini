<!-- Generated: 2026-05-27 | Files scanned: 83 | Token estimate: ~600 -->

# Data

## Entity-Relationship

```
User (1) ──→ (N) Article
Category (1) ──→ (N) Article
Article (1) ──→ (N) ArticleTag ←── (1) Tag
Article (1) ──→ (N) Comment    ←── (1) User
Config (独立表)
```

## Tables

### users
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| username | VARCHAR UNIQUE | 登录名 |
| password | VARCHAR | BCrypt 加密 |
| nickname | VARCHAR | 显示名 |
| email | VARCHAR | 邮箱 |
| avatar | VARCHAR | 头像URL |
| role | VARCHAR | admin / user |
| status | VARCHAR | normal / disabled |
| created_at, updated_at | DATETIME | 自动填充 |

### articles
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| title | VARCHAR | 标题 |
| summary | TEXT | 摘要 |
| content | LONGTEXT | Markdown 正文 |
| cover_image | VARCHAR | 封面图URL |
| category_id | BIGINT FK | 分类 |
| author_id | BIGINT FK | 作者 |
| status | VARCHAR | draft / published / recycled |
| view_count | INT | 浏览量 |
| published_at | DATETIME | 发布时间 |
| created_at, updated_at | DATETIME | 自动填充 |

### categories
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| name, slug | VARCHAR | 名称/别名 |
| created_at, updated_at | DATETIME | 自动填充 |

### tags
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| name, slug | VARCHAR UNIQUE | 名称/别名 |

### article_tags (关联表)
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| article_id | BIGINT FK | 文章 |
| tag_id | BIGINT FK | 标签 |

### comments
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| article_id | BIGINT FK | |
| user_id | BIGINT FK | |
| content | TEXT | |
| status | VARCHAR | pending / approved / rejected |
| created_at, updated_at | DATETIME | |

### configs
| Column | Type | Note |
|--------|------|------|
| id | BIGINT PK | 自增 |
| config_key | VARCHAR | icp_number, copyright 等 |
| config_value | TEXT | |
| created_at, updated_at | DATETIME | |

## Redis Keys

```
article:list:{catId}:{tagId}:{page}:{size}  → PageResult<ArticleVo> (TTL 5min)
article:detail:{id}                          → ArticleVo (TTL 30min)
article:tags:{id}                            → List<TagVo> (TTL 60min, 含空列表)
article:view:{id}                            → Set<user:ip> (TTL 24h, 浏览量去重)
config:public                                → PublicConfigVo (TTL 10min)
refresh_token:{uid}:{uuid}                   → RefreshToken (TTL 7d)
rt_uuid_map                                  → Hash(uuid→key) (TTL 7d, 反向索引)
```

## Dependencies
- **MySQL** — 主数据存储
- **Redis** — 缓存 / 浏览量去重 / Refresh Token 存储 / 分布式锁
- **OSS (阿里云/兼容)** — 文件上传 (头像/封面图)
