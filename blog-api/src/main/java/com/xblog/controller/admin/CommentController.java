package com.xblog.controller.admin;

import com.xblog.dto.QueryAdminCommentDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.CommentService;
import com.xblog.vo.CommentAdminVo;
import com.xblog.vo.CommentStatusVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("adminCommentController")
@RequestMapping("/v1/admin/comments")
@Tag(name = "管理-评论接口", description = "管理员评论管理接口")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Operation(summary = "评论管理列表")
    @GetMapping
    public Result<PageResult<CommentAdminVo>> getCommentPage(@ModelAttribute QueryAdminCommentDto dto) {
        log.info("评论管理列表: page={}, size={}, status={}, articleId={}, userId={}",
                dto.getPage(), dto.getSize(), dto.getStatus(), dto.getArticleId(), dto.getUserId());
        return Result.success(commentService.getAdminCommentPage(dto));
    }

    @Operation(summary = "审核通过评论")
    @PutMapping("/{id}/approve")
    public Result<CommentStatusVo> approveComment(@PathVariable Long id) {
        log.info("审核通过评论: id={}", id);
        return Result.success(commentService.approveComment(id));
    }

    @Operation(summary = "驳回评论")
    @PutMapping("/{id}/reject")
    public Result<CommentStatusVo> rejectComment(@PathVariable Long id) {
        log.info("驳回评论: id={}", id);
        return Result.success(commentService.rejectComment(id));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        log.info("删除评论: id={}", id);
        commentService.deleteCommentAdmin(id);
        return Result.success();
    }
}