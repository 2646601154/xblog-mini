package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.CommentCreateParam;
import com.xblog.dto.CommentUpdateParam;
import com.xblog.dto.QueryAdminCommentDto;
import com.xblog.dto.QueryCommentDto;
import com.xblog.dto.QueryMyCommentDto;
import com.xblog.entity.Comment;
import com.xblog.entity.PageResult;
import com.xblog.vo.CommentAdminVo;
import com.xblog.vo.CommentCreatedVo;
import com.xblog.vo.CommentMyVo;
import com.xblog.vo.CommentPublicVo;
import com.xblog.vo.CommentStatusVo;
import com.xblog.vo.CommentUpdateVo;

public interface CommentService extends IService<Comment> {

    PageResult<CommentPublicVo> getArticleComments(QueryCommentDto dto);

    CommentCreatedVo createComment(CommentCreateParam param);

    PageResult<CommentMyVo> getMyComments(QueryMyCommentDto dto);

    CommentUpdateVo updateComment(Long id, CommentUpdateParam param);

    CommentUpdateVo deleteComment(Long id);

    PageResult<CommentAdminVo> getAdminCommentPage(QueryAdminCommentDto dto);

    CommentStatusVo approveComment(Long id);

    CommentStatusVo rejectComment(Long id);

    void deleteCommentAdmin(Long id);
}