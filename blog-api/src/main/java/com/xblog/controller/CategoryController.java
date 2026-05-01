package com.xblog.controller;

import com.xblog.entity.Category;
import com.xblog.entity.Result;
import com.xblog.service.CategoryService;
import com.xblog.vo.CategoryPublicVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    //获取分类列表 (公开)
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
}
