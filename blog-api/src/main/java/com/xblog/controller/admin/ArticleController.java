package com.xblog.controller.admin;

import com.xblog.dto.AdminQueryArticleDto;
import com.xblog.dto.ArticleCreateParam;
import com.xblog.dto.ArticleUpdateParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.ArticleService;
import com.xblog.vo.ArticleCreateVo;
import com.xblog.vo.ArticlePublishVo;
import com.xblog.vo.ArticleStatusVo;
import com.xblog.vo.ArticleUpdateVo;
import com.xblog.vo.ArticleVo;
import com.xblog.vo.TagVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("adminArticleController")
@RequestMapping("/v1/admin/articles")
@Tag(name = "管理-文章接口", description = "管理员文章管理接口")
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "查询文章管理列表")
    @GetMapping
    public Result<PageResult<ArticleVo>> getAdminArticleList(AdminQueryArticleDto queryDto) {
        log.info("查询文章管理列表: {}", queryDto);
        return Result.success(articleService.getAdminArticlePage(queryDto));
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<ArticleCreateVo> createArticle(@Valid @RequestBody ArticleCreateParam param) {
        log.info("创建文章: {}", param.getTitle());
        return Result.success(articleService.createArticle(param));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<ArticleUpdateVo> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleUpdateParam param) {
        log.info("更新文章: id={}, title={}", id, param.getTitle());
        return Result.success(articleService.updateArticle(id, param));
    }

    @Operation(summary = "发布文章")
    @PutMapping("/{id}/publish")
    public Result<ArticlePublishVo> publishArticle(@PathVariable Long id) {
        log.info("发布文章: id={}", id);
        return Result.success(articleService.publishArticle(id));
    }

    @Operation(summary = "移入回收站")
    @PutMapping("/{id}/recycle")
    public Result<ArticleStatusVo> recycleArticle(@PathVariable Long id) {
        log.info("移入回收站: id={}", id);
        return Result.success(articleService.recycleArticle(id));
    }

    @Operation(summary = "恢复文章")
    @PutMapping("/{id}/restore")
    public Result<ArticleStatusVo> restoreArticle(@PathVariable Long id) {
        log.info("恢复文章: id={}", id);
        return Result.success(articleService.restoreArticle(id));
    }

    @Operation(summary = "彻底删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        log.info("彻底删除文章: id={}", id);
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "绑定文章标签")
    @PostMapping("/{id}/tags")
    public Result<List<TagVo>> bindTags(@PathVariable Long id, @RequestBody List<Long> tagIds) {
        log.info("绑定文章标签: articleId={}, tagIds={}", id, tagIds);
        return Result.success(articleService.bindTags(id, tagIds));
    }
}
