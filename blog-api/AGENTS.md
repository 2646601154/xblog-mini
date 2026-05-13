# blog-api 后端开发指南

**Generated:** 2026-05-05
**父级**: `../AGENTS.md`

---

## API 开发指南

本文档提供 Xblog-mini 后端 API 开发的详细指南。

---

## 接口路径规范

- **PRD/文档** 规定使用 `/api/v1/` 前缀（如 `/api/v1/admin/users`）
- **实际 Controller** 使用 `/v1/` 前缀（如 `@RequestMapping("/v1/admin/users")`）
- **前端请求** 统一使用 `/api/v1/` 前缀
- **Nginx 反向代理** 将 `/api/v1/*` 重写为 `/v1/*` 后转发给后端
- 新增接口时统一按 **代码既有风格 `/v1/`** 编写

---

## 接口开发流程

### 1. 确认接口文档

接口文档位于 `doc/api/` 目录，每个模块有独立文档：
- `auth.md` / `article.md` / `comment.md` / `category.md` / `tag.md` / `user.md` / `config.md`

### 2. 创建/修改文件

标准模块开发需要创建以下层次的文件：

| 层次 | 文件位置 | 示例 |
|------|----------|------|
| DTO | `dto/XxxParam.java` | `CommentCreateParam.java` |
| VO | `vo/XxxVo.java` | `CommentPublicVo.java` |
| Service 接口 | `service/XxxService.java` | `CommentService.java` |
| Service 实现 | `service/impl/XxxServiceImpl.java` | `CommentServiceImpl.java` |
| Controller | `controller/XxxController.java` 或 `controller/admin/XxxController.java` | `CommentController.java` |

### 3. DTO 规范

```java
@Data
public class XxxCreateParam {
    @NotNull(message = "xxx不能为空")
    private Long xxxId;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容最多1000字符")
    private String content;
}
```

### 4. VO 规范

```java
@Data
public class XxxVo {
    private Long id;
    private String content;
    private Map<String, Object> article;  // 使用 Map 避免新建 VO
    private LocalDateTime createdAt;
}
```

### 5. Service 接口规范

```java
public interface XxxService extends IService<Xxx> {
    PageResult<XxxVo> getXxxPage(QueryXxxDto dto);
    XxxVo createXxx(XxxCreateParam param);
}
```

### 6. Controller 规范

```java
@RestController
@RequestMapping("/v1")
@Tag(name = "xxx接口", description = "xxx相关接口")
public class XxxController {

    @Resource
    private XxxService xxxService;

    @Operation(summary = "获取xxx列表")
    @GetMapping("/xxx")
    public Result<PageResult<XxxVo>> getXxxList(@ModelAttribute QueryXxxDto dto) {
        return Result.success(xxxService.getXxxPage(dto));
    }
}
```

---

## 白名单配置

新增公开接口时，需在 `common/config/WebMvcConfig.java` 的 `excludePathPatterns` 中添加路径：

```java
.excludePathPatterns(
    "/v1/auth/login",
    "/v1/auth/register",
    "/v1/categories",
    "/v1/configs",
    "/v1/articles/*/comments",  // 新增公开接口
    // ...
)
```

---

## 批量查询避免 N+1

评论模块是批量查询避免 N+1 的典型案例：

```java
public PageResult<CommentPublicVo> getArticleComments(QueryCommentDto dto) {
    // 1. 先分页查询 Comment 列表
    this.page(page, wrapper);

    // 2. 收集所有 userId
    Set<Long> userIds = comments.stream()
            .map(Comment::getUserId)
            .collect(Collectors.toSet());

    // 3. 批量查询用户
    Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

    // 4. 组装 VO
    List<CommentPublicVo> voList = comments.stream().map(comment -> {
        CommentPublicVo vo = new CommentPublicVo();
        User user = userMap.get(comment.getUserId());
        // ...
        return vo;
    }).toList();
}
```

---

## MyBatis-Plus 分页 null 安全

**每个使用 `new Page<>(page, size)` 的 Service 方法必须做防御**：

```java
int pageNum = dto.getPage() != null ? dto.getPage() : 1;
int pageSize = dto.getSize() != null ? dto.getSize() : 10;
Page<Xxx> page = new Page<>(pageNum, pageSize);
```

---

## 业务错误码（ResultCode）

在 `common/enums/ResultCode.java` 中定义：

| 范围 | 模块 |
|------|------|
| 1000-1999 | 认证模块 |
| 2000-2999 | 用户模块 |
| 3000-3999 | 文章模块 |
| 4000-4999 | 评论模块 |
| 5000-5999 | 分类模块 |
| 6000-6999 | 标签模块 |
| 9000-9999 | 系统级错误 |

---

## 已实现接口清单

### 公开接口（12个）

#### Auth（认证）
| 接口 | 方法 | 路径 |
|------|------|------|
| 用户登录 | POST | `/v1/auth/login` |
| 用户注册 | POST | `/v1/auth/register` |
| 获取当前登录用户信息 | GET | `/v1/auth/me` |

#### Article（文章）
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询文章列表 | GET | `/v1/articles` |
| 获取文章详情 | GET | `/v1/articles/{id}` |
| 获取文章标签 | GET | `/v1/articles/{id}/tags` |
| 获取文章评论列表 | GET | `/v1/articles/{articleId}/comments` |

#### Category（分类）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取分类列表 | GET | `/v1/categories` |

#### Tag（标签）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取标签列表 | GET | `/v1/tags` |

#### Config（配置）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取公开配置 | GET | `/v1/configs` |

#### Comment（评论）
| 接口 | 方法 | 路径 |
|------|------|------|
| 发表评论 | POST | `/v1/comments` |
| 我的评论列表 | GET | `/v1/comments/my` |
| 编辑评论 | PUT | `/v1/comments/{id}` |
| 删除评论 | DELETE | `/v1/comments/{id}` |

---

### 管理端接口（22个）

#### Article（管理-文章）
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询文章管理列表 | GET | `/v1/admin/articles` |
| 创建文章 | POST | `/v1/admin/articles` |
| 更新文章 | PUT | `/v1/admin/articles/{id}` |
| 发布文章 | PUT | `/v1/admin/articles/{id}/publish` |
| 移入回收站 | PUT | `/v1/admin/articles/{id}/recycle` |
| 恢复文章 | PUT | `/v1/admin/articles/{id}/restore` |
| 彻底删除文章 | DELETE | `/v1/admin/articles/{id}` |
| 绑定文章标签 | POST | `/v1/admin/articles/{id}/tags` |

#### Category（管理-分类）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取管理分类列表 | GET | `/v1/admin/categories` |
| 创建分类 | POST | `/v1/admin/categories` |
| 更新分类 | PUT | `/v1/admin/categories/{id}` |
| 删除分类 | DELETE | `/v1/admin/categories/{id}` |

#### Comment（管理-评论）
| 接口 | 方法 | 路径 |
|------|------|------|
| 评论管理列表 | GET | `/v1/admin/comments` |
| 审核通过评论 | PUT | `/v1/admin/comments/{id}/approve` |
| 驳回评论 | PUT | `/v1/admin/comments/{id}/reject` |
| 删除评论 | DELETE | `/v1/admin/comments/{id}` |

#### Config（管理-配置）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取所有配置 | GET | `/v1/admin/configs` |
| 更新配置 | PUT | `/v1/admin/configs` |
| 获取单个配置 | GET | `/v1/admin/configs/{key}` |

#### Tag（管理-标签）
| 接口 | 方法 | 路径 |
|------|------|------|
| 获取管理标签列表 | GET | `/v1/admin/tags` |
| 创建标签 | POST | `/v1/admin/tags` |
| 更新标签 | PUT | `/v1/admin/tags/{id}` |
| 删除标签 | DELETE | `/v1/admin/tags/{id}` |

#### User（管理-用户）
| 接口 | 方法 | 路径 |
|------|------|------|
| 查询用户列表 | GET | `/v1/admin/users` |
| 查询用户详情 | GET | `/v1/admin/users/{id}` |
| 更新用户 | PUT | `/v1/admin/users/{id}` |
| 禁用用户 | PUT | `/v1/admin/users/{id}/disable` |
| 启用用户 | PUT | `/v1/admin/users/{id}/enable` |
| 删除用户 | DELETE | `/v1/admin/users/{id}` |

---

## 安全注意事项

- `application.yml` 中 JWT secret 为占位值 `your-secret-key-change-in-production`，生产环境必须替换
- `UserContext` 使用 ThreadLocal 存储当前用户信息，需在请求链路终点（如拦截器 `afterCompletion`）调用 `UserContext.clear()` 防止内存泄漏
- 存在空的 `com.xblog.xblog` 包目录，可清理

## 文档同步

开发新接口时，同步更新 `doc/api/` 下对应模块的 Markdown 文档：
- 请求/响应格式
- 业务说明
- 错误码说明