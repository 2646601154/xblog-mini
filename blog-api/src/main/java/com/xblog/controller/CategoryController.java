package com.xblog.controller;

import com.xblog.entity.Category;
import com.xblog.entity.Result;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryPublicVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/categories")
@Tag(name = "分类接口", description = "公开分类相关接口")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<CategoryPublicVo>> getCategoryList(){
        log.info("获取分类列表");
        List<Category> categories = categoryService.list();
        List<CategoryPublicVo> voList = categories.stream().map(c -> {
            CategoryPublicVo vo = new CategoryPublicVo();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSlug(c.getSlug());
            vo.setDescription(c.getDescription());
            vo.setSortOrder(c.getSortOrder());
            return vo;
        }).toList();
        return Result.success(voList);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{slug}")
    public Result<CategoryPublicVo> getCategoryBySlug(@PathVariable String slug) {
        log.info("获取分类详情: slug={}", slug);
        return Result.success(categoryService.getCategoryBySlug(slug));
    }
}
