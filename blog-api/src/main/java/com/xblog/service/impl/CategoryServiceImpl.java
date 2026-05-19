package com.xblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.dto.CategoryCreateParam;
import com.xblog.dto.CategoryUpdateParam;
import com.xblog.entity.Article;
import com.xblog.entity.Category;
import com.xblog.mapper.CategoryMapper;
import com.xblog.service.ArticleService;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.CategoryPublicVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleService articleService;

    public CategoryServiceImpl(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public List<CategoryAdminVo> getAdminCategoryList() {
        return baseMapper.getAdminCategoryList();
    }

    @Override
    public CategoryPublicVo createCategory(CategoryCreateParam param) {
        // 校验分类名称是否已存在
        if (lambdaQuery().eq(Category::getName, param.getName()).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_EXISTS);
        }

        // 校验分类slug是否已存在
        if (lambdaQuery().eq(Category::getSlug, param.getSlug()).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_SLUG_EXISTS);
        }

        Category category = new Category();
        category.setName(param.getName());
        category.setSlug(param.getSlug());
        category.setDescription(param.getDescription());
        category.setSortOrder(param.getSortOrder() != null ? param.getSortOrder() : 0);

        save(category);

        CategoryPublicVo vo = new CategoryPublicVo();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());

        return vo;
    }

    @Override
    public CategoryPublicVo updateCategory(Long id, CategoryUpdateParam param) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 校验分类名称是否与其他分类重复
        if (lambdaQuery().eq(Category::getName, param.getName()).ne(Category::getId, id).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_EXISTS);
        }

        // 校验分类slug是否与其他分类重复
        if (lambdaQuery().eq(Category::getSlug, param.getSlug()).ne(Category::getId, id).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_SLUG_EXISTS);
        }

        category.setName(param.getName());
        category.setSlug(param.getSlug());
        category.setDescription(param.getDescription());
        if (param.getSortOrder() != null) {
            category.setSortOrder(param.getSortOrder());
        }

        updateById(category);

        CategoryPublicVo vo = new CategoryPublicVo();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());

        return vo;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        long articleCount = articleService.lambdaQuery()
                .eq(Article::getCategoryId, id)
                .eq(Article::getDeleted, false)
                .count();

        if (articleCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_ARTICLES);
        }

        articleService.lambdaUpdate()
                .eq(Article::getCategoryId, id)
                .set(Article::getCategoryId, null)
                .update();

        removeById(id);
    }

    @Override
    public CategoryPublicVo getCategoryBySlug(String slug) {
        Category category = lambdaQuery().eq(Category::getSlug, slug).one();
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 查询该分类下的文章数量
        long articleCount = articleService.lambdaQuery()
                .eq(Article::getCategoryId, category.getId())
                .eq(Article::getDeleted, false)
                .count();

        CategoryPublicVo vo = new CategoryPublicVo();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());

        return vo;
    }
}
