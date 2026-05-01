package com.xblog.controller.admin;

import com.xblog.dto.CategoryCreateParam;
import com.xblog.entity.Result;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryAdminVo;
import com.xblog.vo.CategoryPublicVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("adminCategoryController")
@RequestMapping("/v1/admin/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取管理分类列表
     *
     * @return
     */
    @GetMapping
    public Result<List<CategoryAdminVo>> getCategoryList(){
        log.info("获取管理分类列表");
        return Result.success(categoryService.getAdminCategoryList());
    }

    //创建分类
    @PostMapping
    public Result<CategoryPublicVo> createCategory(@Valid @RequestBody CategoryCreateParam param){
        log.info("创建分类: {}", param.getName());
        return Result.success(categoryService.createCategory(param));
    }
}
