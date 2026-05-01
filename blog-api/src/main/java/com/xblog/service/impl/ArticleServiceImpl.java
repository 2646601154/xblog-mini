package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.*;
import com.xblog.mapper.*;
import com.xblog.service.ArticleService;
import com.xblog.vo.ArticleVo;
import com.xblog.vo.AuthorVo;
import com.xblog.vo.CategoryPublicVo;
import com.xblog.vo.TagVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;

    @Override
    public PageResult<ArticleVo> getPublicArticlePage(QueryArticleDto queryDto) {
        // 1. 构建分页对象
        Page<Article> page = new Page<>(queryDto.getPage(), queryDto.getSize());

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
                .filter(id -> id != null)
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
