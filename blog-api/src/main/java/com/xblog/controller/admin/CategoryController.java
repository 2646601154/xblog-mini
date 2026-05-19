package com.xblog.controller.admin;

import com.xblog.dto.CategoryCreateParam;
import com.xblog.dto.CategoryUpdateParam;
import com.xblog.entity.Result;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.CategoryPublicVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("adminCategoryController")
@RequestMapping("/v1/admin/categories")
@Tag(name = "管理-分类接口", description = "管理员分类管理接口")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "获取管理分类列表")
    @GetMapping
    public Result<List<CategoryAdminVo>> getCategoryList() {
        log.info("获取管理分类列表");
        return Result.success(categoryService.getAdminCategoryList());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<CategoryPublicVo> createCategory(@Valid @RequestBody CategoryCreateParam param) {
        log.info("创建分类: {}", param.getName());
        return Result.success(categoryService.createCategory(param));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<CategoryPublicVo> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpdateParam param) {
        log.info("更新分类: id={}, name={}", id, param.getName());
        return Result.success(categoryService.updateCategory(id, param));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: id={}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }
}