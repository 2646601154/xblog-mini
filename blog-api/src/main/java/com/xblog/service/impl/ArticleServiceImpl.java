package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.util.IpUtil;
import com.xblog.common.util.RedisUtil;
import com.xblog.common.util.PageUtil;
import com.xblog.common.util.UserContext;
import com.xblog.dto.AdminQueryArticleDto;
import com.xblog.dto.ArticleCreateParam;
import com.xblog.dto.ArticleUpdateParam;
import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.*;
import com.xblog.mapper.*;
import com.xblog.service.ArticleService;
import com.xblog.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final RedisUtil redisUtil;
    private final IpUtil ipUtil;

    // ============================= Cache Constants ============================

    private static final String CACHE_KEY_PUBLIC_LIST = "article:list:";
    private static final String CACHE_KEY_DETAIL = "article:detail:";
    private static final String CACHE_KEY_TAGS = "article:tags:";

    private static final long TTL_PUBLIC_LIST_MINUTES = 5;
    private static final long TTL_DETAIL_MINUTES = 30;
    private static final long TTL_TAGS_MINUTES = 60;

    public ArticleServiceImpl(CategoryMapper categoryMapper,
                              UserMapper userMapper,
                              TagMapper tagMapper,
                              ArticleTagMapper articleTagMapper,
                              RedisUtil redisUtil,
                              IpUtil ipUtil) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.redisUtil = redisUtil;
        this.ipUtil = ipUtil;
    }

    @Override
    public PageResult<ArticleVo> getPublicArticlePage(QueryArticleDto queryDto) {
        String cacheKey = buildPublicListKey(queryDto);

        // 1. 尝试读取缓存
        try {
            PageResult<ArticleVo> cached = redisUtil.get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 - 公开文章列表: {}", cacheKey);
                return cached;
            }
        } catch (Exception e) {
            log.warn("缓存读取失败，回退到数据库: {}", cacheKey, e);
        }

        // 2. 缓存未命中，查询数据库
        PageResult<ArticleVo> result = queryPublicArticlePage(queryDto);

        // 3. 写入缓存
        try {
            redisUtil.set(cacheKey, result, TTL_PUBLIC_LIST_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存写入失败: {}", cacheKey, e);
        }

        return result;
    }

    /**
     * 从数据库查询公开文章列表（无缓存）
     */
    private PageResult<ArticleVo> queryPublicArticlePage(QueryArticleDto queryDto) {
        // 1. 构建分页对象
        int pageNum = PageUtil.pageNum(queryDto.getPage());
        int pageSize = PageUtil.pageSize(queryDto.getSize());
        Page<Article> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 只查询已发布的文章
        wrapper.eq(Article::getStatus, "published");
        // 按分类筛选
        if (queryDto.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, queryDto.getCategoryId());
        }
        // 按标签筛选需要关联查询
        if (queryDto.getTagId() != null) {
            // 先获取该标签关联的所有文章ID
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getTagId, queryDto.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            if (articleTags.isEmpty()) {
                // 没有文章关联该标签，返回空结果
                return new PageResult<>();
            }
            Set<Long> articleIds = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toSet());
            wrapper.in(Article::getId, articleIds);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedAt);

        // 3. 执行分页查询
        Page<Article> resultPage = page(page, wrapper);

        // 4. 转换为 VO 并构建分页结果
        PageResult<ArticleVo> pageResult = new PageResult<>();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage((int) resultPage.getCurrent());
        pageResult.setSize((int) resultPage.getSize());
        pageResult.setRecords(convertToArticleVoList(resultPage.getRecords()));

        return pageResult;
    }

    /**
     * 构建公开文章列表缓存 key
     */
    private String buildPublicListKey(QueryArticleDto dto) {
        return CACHE_KEY_PUBLIC_LIST
                + (dto.getCategoryId() != null ? dto.getCategoryId() : "all")
                + ":"
                + (dto.getTagId() != null ? dto.getTagId() : "all")
                + ":"
                + (dto.getPage() != null ? dto.getPage() : 1)
                + ":"
                + (dto.getSize() != null ? dto.getSize() : 10);
    }

    @Override
    public ArticleVo getArticleDetail(Long id) {
        // BUG-4: 公开详情接口每次访问会增加浏览量；管理端编辑请使用 getAdminArticleDetail
        String cacheKey = CACHE_KEY_DETAIL + id;

        // 1. 尝试读取缓存
        try {
            ArticleVo cached = redisUtil.get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 - 文章详情: {}", cacheKey);
                // 缓存命中时仍然记录浏览量（独立于缓存）
                boolean incr = incrementViewCount(id);
                cached.setViewCount(cached.getViewCount() + (incr ? 1 : 0));
                return cached;
            }
        } catch (Exception e) {
            log.warn("缓存读取失败，回退到数据库: {}", cacheKey, e);
        }

        // 2. 缓存未命中，查询数据库
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 公开接口：只返回已发布文章
        if (!"published".equals(article.getStatus())) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 3. 增加浏览量
        boolean incr = incrementViewCount(id);

        // 4. 组装 VO
        List<ArticleVo> voList = convertToArticleVoList(Collections.singletonList(article));
        ArticleVo vo = voList.get(0);

        // 设置详情专属字段
        vo.setContent(article.getContent());
        vo.setStatus(article.getStatus());
        vo.setUpdatedAt(article.getUpdatedAt());
        vo.setViewCount(article.getViewCount() + (incr ? 1 : 0));

        // 5. 写入缓存
        try {
            redisUtil.set(cacheKey, vo, TTL_DETAIL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存写入失败: {}", cacheKey, e);
        }

        return vo;
    }

    @Override
    public ArticlePrevNextVo getArticlePrevNext(Long id) {
        // 先验证当前文章存在且已发布
        Article current = getById(id);
        if (current == null || !"published".equals(current.getStatus())) {
            return new ArticlePrevNextVo();
        }

        ArticlePrevNextVo vo = new ArticlePrevNextVo();

        // 上一篇：id < currentId 且 published，按 id 降序取第一条
        Article prev = lambdaQuery()
                .eq(Article::getStatus, "published")
                .lt(Article::getId, id)
                .orderByDesc(Article::getId)
                .last("LIMIT 1")
                .one();

        if (prev != null) {
            ArticlePrevNextVo.ArticleBriefVo prevVo = new ArticlePrevNextVo.ArticleBriefVo();
            prevVo.setId(prev.getId());
            prevVo.setTitle(prev.getTitle());
            vo.setPrevious(prevVo);
        }

        // 下一篇：id > currentId 且 published，按 id 升序取第一条
        Article next = lambdaQuery()
                .eq(Article::getStatus, "published")
                .gt(Article::getId, id)
                .orderByAsc(Article::getId)
                .last("LIMIT 1")
                .one();

        if (next != null) {
            ArticlePrevNextVo.ArticleBriefVo nextVo = new ArticlePrevNextVo.ArticleBriefVo();
            nextVo.setId(next.getId());
            nextVo.setTitle(next.getTitle());
            vo.setNext(nextVo);
        }

        return vo;
    }

    /**
     * 增加文章浏览量（基于 Redis Set 防重复计数）
     *
     * @return true 如果 DB 浏览量实际增加了
     */
    private boolean incrementViewCount(Long articleId) {
        String viewKey = "article:view:" + articleId;
        String viewerId = getViewerId();
        long isNewView = redisUtil.sAdd(viewKey, viewerId);
        if (isNewView > 0) {
            lambdaUpdate()
                    .eq(Article::getId, articleId)
                    .setSql("view_count = view_count + 1")
                    .update();
            redisUtil.expire(viewKey, 24, TimeUnit.HOURS);
            return true;
        }
        return false;
    }

    /**
     * 获取当前访问者标识（用户ID 或 IP）
     */
    private String getViewerId() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + ipUtil.getClientIp();
    }

    @Override
    public List<TagVo> getArticleTags(Long id) {
        String cacheKey = CACHE_KEY_TAGS + id;

        // 1. 尝试读取缓存
        try {
            List<TagVo> cached = redisUtil.get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 - 文章标签: {}", cacheKey);
                return cached;
            }
        } catch (Exception e) {
            log.warn("缓存读取失败，回退到数据库: {}", cacheKey, e);
        }

        // 2. 缓存未命中，查询数据库
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 查询文章标签关联
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id)
        );

        // 无标签关联则缓存空列表（防止缓存穿透）
        if (articleTags.isEmpty()) {
            List<TagVo> emptyList = Collections.emptyList();
            try {
                redisUtil.set(cacheKey, emptyList, TTL_TAGS_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("缓存写入失败: {}", cacheKey, e);
            }
            return emptyList;
        }

        // 批量查询标签
        Set<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toSet());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        // 转换为 TagVo
        List<TagVo> result = tagIds.stream()
                .map(tagMap::get)
                .filter(tag -> tag != null)
                .map(tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    vo.setSlug(tag.getSlug());
                    return vo;
                })
                .collect(Collectors.toList());

        // 3. 写入缓存
        try {
            redisUtil.set(cacheKey, result, TTL_TAGS_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存写入失败: {}", cacheKey, e);
        }

        return result;
    }

    @Override
    public PageResult<ArticleVo> getAdminArticlePage(AdminQueryArticleDto queryDto) {
        // 1. 构建分页对象
        int pageNum = PageUtil.pageNum(queryDto.getPage());
        int pageSize = PageUtil.pageSize(queryDto.getSize());
        Page<Article> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 按状态筛选（可选）
        if (queryDto.getStatus() != null && !queryDto.getStatus().trim().isEmpty()) {
            wrapper.eq(Article::getStatus, queryDto.getStatus());
        }

        // 按分类筛选（可选）
        if (queryDto.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, queryDto.getCategoryId());
        }

        // 标题模糊搜索（可选）
        if (queryDto.getTitle() != null && !queryDto.getTitle().trim().isEmpty()) {
            wrapper.like(Article::getTitle, queryDto.getTitle());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedAt);

        // 3. 执行分页查询
        Page<Article> resultPage = page(page, wrapper);

        // 4. 转换为 VO（复用已有方法，包含 tags）
        List<ArticleVo> voList = convertToArticleVoList(resultPage.getRecords());

        // 5. 构建分页结果
        PageResult<ArticleVo> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage((int) resultPage.getCurrent());
        pageResult.setSize((int) resultPage.getSize());

        return pageResult;
    }

    @Override
    public ArticleVo getAdminArticleDetail(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        List<ArticleVo> voList = convertToArticleVoList(Collections.singletonList(article));
        ArticleVo vo = voList.get(0);
        vo.setContent(article.getContent());
        vo.setUpdatedAt(article.getUpdatedAt());
        return vo;
    }

    @Override
    public ArticleCreateVo createArticle(ArticleCreateParam param) {
        // 校验分类是否存在
        Category category = categoryMapper.selectById(param.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 校验状态是否有效
        String status = param.getStatus() != null ? param.getStatus() : "draft";
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException(ResultCode.ARTICLE_STATUS_INVALID);
        }

        // 校验标签是否存在
        if (param.getTagIds() != null && !param.getTagIds().isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(param.getTagIds());
            if (tags.size() != param.getTagIds().size()) {
                throw new BusinessException(ResultCode.TAG_NOT_FOUND);
            }
        }

        // 创建文章
        Article article = new Article();
        article.setTitle(param.getTitle());
        article.setSummary(param.getSummary());
        article.setContent(param.getContent());
        article.setCoverImage(param.getCoverImage());
        article.setCategoryId(param.getCategoryId());
        // 从当前登录用户获取作者ID
        article.setAuthorId(UserContext.getUserId());
        article.setStatus(status);
        article.setViewCount(0);
        if ("published".equals(status)) {
            article.setPublishedAt(LocalDateTime.now());
        }
        save(article);

        // 清理文章列表缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");

        // 保存文章标签关联
        if (param.getTagIds() != null && !param.getTagIds().isEmpty()) {
            List<ArticleTag> articleTags = param.getTagIds().stream()
                    .map(tagId -> {
                        ArticleTag at = new ArticleTag();
                        at.setArticleId(article.getId());
                        at.setTagId(tagId);
                        return at;
                    })
                    .toList();
            articleTags.forEach(articleTagMapper::insert);
        }

        // 返回结果
        ArticleCreateVo vo = new ArticleCreateVo();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setStatus(article.getStatus());
        vo.setCreatedAt(article.getCreatedAt());
        return vo;
    }

    @Override
    public ArticleUpdateVo updateArticle(Long id, ArticleUpdateParam param) {
        // 查询文章是否存在
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // admin 可以编辑所有文章，普通用户只能编辑自己的文章
        String role = UserContext.getRole();
        if (!"admin".equals(role) && !article.getAuthorId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.ARTICLE_CANNOT_EDIT_OTHERS);
        }

        // 校验分类是否存在
        Category category = categoryMapper.selectById(param.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 校验标签是否存在
        if (param.getTagIds() != null && !param.getTagIds().isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(param.getTagIds());
            if (tags.size() != param.getTagIds().size()) {
                throw new BusinessException(ResultCode.TAG_NOT_FOUND);
            }
        }

        // 更新文章
        article.setTitle(param.getTitle());
        article.setSummary(param.getSummary());
        article.setContent(param.getContent());
        article.setCoverImage(param.getCoverImage());
        article.setCategoryId(param.getCategoryId());
        if (param.getStatus() != null && ("draft".equals(param.getStatus()) || "published".equals(param.getStatus()))) {
            article.setStatus(param.getStatus());
        }

        updateById(article);

        // 清理文章列表和详情缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);
        // 清理文章标签缓存
        redisUtil.delete(CACHE_KEY_TAGS + id);

        // 更新文章标签关联（先删后插）
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id)
        );
        if (param.getTagIds() != null && !param.getTagIds().isEmpty()) {
            List<ArticleTag> articleTags = param.getTagIds().stream()
                    .map(tagId -> {
                        ArticleTag at = new ArticleTag();
                        at.setArticleId(id);
                        at.setTagId(tagId);
                        return at;
                    })
                    .toList();
            articleTags.forEach(articleTagMapper::insert);
        }

        // 返回结果
        ArticleUpdateVo vo = new ArticleUpdateVo();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setStatus(article.getStatus());
        vo.setUpdatedAt(article.getUpdatedAt());
        return vo;
    }

    @Override
    public ArticlePublishVo publishArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        if ("recycled".equals(article.getStatus())) {
            throw new BusinessException(ResultCode.ARTICLE_IN_RECYCLE_CANNOT_PUBLISH);
        }

        LocalDateTime now = LocalDateTime.now();
        article.setStatus("published");
        article.setPublishedAt(now);
        updateById(article);

        // 清理文章列表和详情缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);

        ArticlePublishVo vo = new ArticlePublishVo();
        vo.setId(article.getId());
        vo.setStatus(article.getStatus());
        vo.setPublishedAt(article.getPublishedAt());
        return vo;
    }

    @Override
    public ArticleStatusVo recycleArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        if ("recycled".equals(article.getStatus())) {
            throw new BusinessException(ResultCode.ARTICLE_ALREADY_RECYCLED);
        }

        article.setStatus("recycled");
        updateById(article);

        // 清理文章列表和详情缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);

        ArticleStatusVo vo = new ArticleStatusVo();
        vo.setId(article.getId());
        vo.setStatus(article.getStatus());
        return vo;
    }

    @Override
    public ArticleStatusVo restoreArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        if (!"recycled".equals(article.getStatus())) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_IN_RECYCLE_CANNOT_RESTORE);
        }

        article.setStatus("draft");
        updateById(article);

        // 清理文章列表和详情缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);

        ArticleStatusVo vo = new ArticleStatusVo();
        vo.setId(article.getId());
        vo.setStatus(article.getStatus());
        return vo;
    }

    @Override
    public void deleteArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        String role = UserContext.getRole();
        if (!"admin".equals(role) && !article.getAuthorId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.ARTICLE_CANNOT_DELETE_OTHERS);
        }

        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id)
        );

        removeById(id);

        // 清理文章列表和详情缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);
        // 清理浏览量记录和标签缓存
        redisUtil.delete("article:view:" + id);
        redisUtil.delete(CACHE_KEY_TAGS + id);
    }

    @Override
    public List<TagVo> bindTags(Long id, List<Long> tagIds) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new BusinessException(ResultCode.TAG_NOT_FOUND);
            }
        }

        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id)
        );

        if (tagIds != null && !tagIds.isEmpty()) {
            List<ArticleTag> articleTags = tagIds.stream()
                    .map(tagId -> {
                        ArticleTag at = new ArticleTag();
                        at.setArticleId(id);
                        at.setTagId(tagId);
                        return at;
                    })
                    .toList();
            articleTags.forEach(articleTagMapper::insert);
        }

        // 清理文章列表、详情和标签缓存
        redisUtil.deleteByPatternNonBlocking("article:list:*");
        redisUtil.delete("article:detail:" + id);
        redisUtil.delete(CACHE_KEY_TAGS + id);

        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        return tagMapper.selectBatchIds(tagIds).stream()
                .map(tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    vo.setSlug(tag.getSlug());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private List<ArticleVo> convertToArticleVoList(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集需要查询的ID
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = articles.stream()
                .map(Article::getId)
                .collect(Collectors.toSet());

        // 批量查询分类
        LambdaQueryWrapper<Category> categoryQueryWrapper = new LambdaQueryWrapper<>();
        categoryQueryWrapper.in(Category::getId, categoryIds);
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectList(categoryQueryWrapper).stream()
                  .collect(Collectors.toMap(Category::getId, c -> c));

        // 批量查询作者
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, authorIds);
        Map<Long, User> authorMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectList(userQueryWrapper).stream()
                  .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询文章标签关联
        LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        articleTagWrapper.in(ArticleTag::getArticleId, articleIds);
        List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagWrapper);
        Set<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toSet());

        // 批量查询标签
        LambdaQueryWrapper<Tag> tagQueryWrapper = new LambdaQueryWrapper<>();
        tagQueryWrapper.in(Tag::getId, tagIds);
        Map<Long, Tag> tagMap = tagIds.isEmpty()
                ? Collections.emptyMap()
                : tagMapper.selectList(tagQueryWrapper).stream()
                  .collect(Collectors.toMap(Tag::getId, t -> t));

        // 按文章ID分组标签
        Map<Long, List<ArticleTag>> articleTagMap = articleTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId));

        // 转换为 VO
        return articles.stream().map(article -> {
            ArticleVo vo = new ArticleVo();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCoverImage(article.getCoverImage());
            vo.setViewCount(article.getViewCount());
            vo.setPublishedAt(article.getPublishedAt());
            vo.setCreatedAt(article.getCreatedAt());
            vo.setStatus(article.getStatus());

            // 设置分类
            if (article.getCategoryId() != null && categoryMap.containsKey(article.getCategoryId())) {
                Category category = categoryMap.get(article.getCategoryId());
                CategoryPublicVo categoryVo = new CategoryPublicVo();
                categoryVo.setId(category.getId());
                categoryVo.setName(category.getName());
                categoryVo.setSlug(category.getSlug());
                vo.setCategory(categoryVo);
            }

            // 设置作者
            if (article.getAuthorId() != null && authorMap.containsKey(article.getAuthorId())) {
                User user = authorMap.get(article.getAuthorId());
                AuthorVo authorVo = new AuthorVo();
                authorVo.setId(user.getId());
                authorVo.setUsername(user.getUsername());
                authorVo.setNickname(user.getNickname());
                authorVo.setAvatar(user.getAvatar());
                vo.setAuthor(authorVo);
            }

            // 设置标签
            List<ArticleTag> tags = articleTagMap.getOrDefault(article.getId(), Collections.emptyList());
            List<TagVo> tagVoList = tags.stream()
                    .map(at -> {
                        Tag tag = tagMap.get(at.getTagId());
                        if (tag == null) return null;
                        TagVo tagVo = new TagVo();
                        tagVo.setId(tag.getId());
                        tagVo.setName(tag.getName());
                        tagVo.setSlug(tag.getSlug());
                        return tagVo;
                    })
                    .filter(t -> t != null)
                    .collect(Collectors.toList());
            vo.setTags(tagVoList);

            return vo;
        }).collect(Collectors.toList());
    }
}
