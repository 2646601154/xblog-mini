package com.xblog.controller;

import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.ArticleService;
import com.xblog.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/v1/articles")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 查询文章列表
     * @param queryArticleDto
     * @return
     */
    @GetMapping
    public Result<PageResult<ArticleVo>> queryArticleList(QueryArticleDto queryArticleDto) {
        log.info("查询文章列表: {}", queryArticleDto);
        return Result.success(articleService.getPublicArticlePage(queryArticleDto));
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVo> getArticleDetail(@PathVariable Long id) {
        log.info("获取文章详情: {}", id);
        return Result.success(articleService.getArticleDetail(id));
    }

}
