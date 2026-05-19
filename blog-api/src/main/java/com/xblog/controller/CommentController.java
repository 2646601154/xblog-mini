package com.xblog.controller;

import com.xblog.dto.CommentCreateParam;
import com.xblog.dto.CommentUpdateParam;
import com.xblog.dto.QueryMyCommentDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.CommentService;
import com.xblog.vo.CommentCreatedVo;
import com.xblog.vo.CommentMyVo;
import com.xblog.vo.CommentUpdateVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/comments")
@Tag(name = "评论接口", description = "评论相关接口")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentCreatedVo> createComment(@Valid @RequestBody CommentCreateParam param) {
        log.info("发表评论: articleId={}, content={}", param.getArticleId(), param.getContent());
        return Result.success(commentService.createComment(param));
    }

    @Operation(summary = "我的评论列表")
    @GetMapping("/my")
    public Result<PageResult<CommentMyVo>> getMyComments(@ModelAttribute QueryMyCommentDto dto) {
        log.info("我的评论列表: page={}, size={}, status={}", dto.getPage(), dto.getSize(), dto.getStatus());
        return Result.success(commentService.getMyComments(dto));
    }

    @Operation(summary = "编辑评论")
    @PutMapping("/{id}")
    public Result<CommentUpdateVo> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateParam param) {
        log.info("编辑评论: id={}, content={}", id, param.getContent());
        return Result.success(commentService.updateComment(id, param));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<CommentUpdateVo> deleteComment(@PathVariable Long id) {
        log.info("删除评论: id={}", id);
        return Result.success(commentService.deleteComment(id));
    }
}