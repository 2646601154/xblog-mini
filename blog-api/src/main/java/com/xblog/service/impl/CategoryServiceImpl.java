package com.xblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.dto.CategoryCreateParam;
import com.xblog.entity.Category;
import com.xblog.mapper.CategoryMapper;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.CategoryPublicVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

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
}
