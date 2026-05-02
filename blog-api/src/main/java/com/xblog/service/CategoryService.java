package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.CategoryCreateParam;
import com.xblog.dto.CategoryUpdateParam;
import com.xblog.entity.Category;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.CategoryPublicVo;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<CategoryAdminVo> getAdminCategoryList();

    CategoryPublicVo createCategory(CategoryCreateParam param);

    CategoryPublicVo updateCategory(Long id, CategoryUpdateParam param);

    void deleteCategory(Long id);

    CategoryPublicVo getCategoryBySlug(String slug);
}
