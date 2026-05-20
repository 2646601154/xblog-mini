package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.CommentStatus;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.util.PageUtil;
import com.xblog.common.util.UserContext;
import com.xblog.dto.CommentCreateParam;
import com.xblog.dto.CommentUpdateParam;
import com.xblog.dto.QueryAdminCommentDto;
import com.xblog.dto.QueryCommentDto;
import com.xblog.dto.QueryMyCommentDto;
import com.xblog.entity.Article;
import com.xblog.entity.Comment;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.mapper.CommentMapper;
import com.xblog.mapper.UserMapper;
import com.xblog.service.ArticleService;
import com.xblog.service.CommentService;
import com.xblog.vo.AuthorVo;
import com.xblog.vo.CommentAdminVo;
import com.xblog.vo.CommentCreatedVo;
import com.xblog.vo.CommentMyVo;
import com.xblog.vo.CommentPublicVo;
import com.xblog.vo.CommentStatusVo;
import com.xblog.vo.CommentUpdateVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserMapper userMapper;
    private final ArticleService articleService;

    public CommentServiceImpl(UserMapper userMapper, ArticleService articleService) {
        this.userMapper = userMapper;
        this.articleService = articleService;
    }

    @Override
    public PageResult<CommentPublicVo> getArticleComments(QueryCommentDto dto) {
        int pageNum = PageUtil.pageNum(dto.getPage());
        int pageSize = PageUtil.pageSize(dto.getSize());
        Page<Comment> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, dto.getArticleId())
                .eq(Comment::getStatus, CommentStatus.APPROVED.getValue())
                .orderByDesc(Comment::getCreatedAt);

        this.page(page, wrapper);

        // 批量查询用户，避免 N+1 问题
        List<Comment> comments = page.getRecords();
        if (comments.isEmpty()) {
            return PageUtil.build(page, List.of());
        }

        // 批量查询文章和用户，避免 N+1 问题
        Set<Long> articleIds = comments.stream()
                .map(Comment::getArticleId)
                .collect(Collectors.toSet());

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, Article> articleMap = articleService.listByIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CommentAdminVo> voList = comments.stream().map(comment -> {
            CommentAdminVo vo = new CommentAdminVo();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setStatus(comment.getStatus());
            vo.setCreatedAt(comment.getCreatedAt());

            Article article = articleMap.get(comment.getArticleId());
            if (article != null) {
                vo.setArticle(Map.of("id", article.getId(), "title", article.getTitle()));
            }

            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUser(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "nickname", user.getNickname()
                ));
            }
            return vo;
        }).toList();

        return PageUtil.build(page, voList);
    }

    @Override
    public CommentCreatedVo createComment(CommentCreateParam param) {
        Article article = articleService.getById(param.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        Long userId = UserContext.getUserId();

        Comment comment = new Comment();
        comment.setArticleId(param.getArticleId());
        comment.setUserId(userId);
        comment.setContent(param.getContent());
        comment.setStatus(CommentStatus.PENDING.getValue());
        this.save(comment);

        CommentCreatedVo vo = new CommentCreatedVo();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }

    @Override
    public PageResult<CommentMyVo> getMyComments(QueryMyCommentDto dto) {
        int pageNum = PageUtil.pageNum(dto.getPage());
        int pageSize = PageUtil.pageSize(dto.getSize());
        Page<Comment> page = new Page<>(pageNum, pageSize);

        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
                .eq(StringUtils.hasText(dto.getStatus()), Comment::getStatus, dto.getStatus())
                .orderByDesc(Comment::getCreatedAt);

        this.page(page, wrapper);

        List<Comment> comments = page.getRecords();
        if (comments.isEmpty()) {
            return PageUtil.build(page, List.of());
        }

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CommentPublicVo> voList = comments.stream().map(comment -> {
            CommentPublicVo vo = new CommentPublicVo();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());

            User user = userMap.get(comment.getUserId());
            if (user != null) {
                AuthorVo authorVo = new AuthorVo();
                BeanUtils.copyProperties(user, authorVo);
                vo.setUser(authorVo);
            }
            return vo;
        }).toList();

        return PageUtil.build(page, voList);
    }

    @Override
    public CommentUpdateVo updateComment(Long id, CommentUpdateParam param) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        Long userId = UserContext.getUserId();
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COMMENT_CANNOT_EDIT_OTHERS);
        }

        comment.setContent(param.getContent());
        comment.setStatus(CommentStatus.PENDING.getValue());
        this.updateById(comment);

        CommentUpdateVo vo = new CommentUpdateVo();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }

    @Override
    public CommentUpdateVo deleteComment(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        Long userId = UserContext.getUserId();
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COMMENT_CANNOT_DELETE_OTHERS);
        }

        if (CommentStatus.APPROVED.getValue().equals(comment.getStatus())) {
            throw new BusinessException(ResultCode.COMMENT_APPROVED_CANNOT_DELETE);
        }

        this.removeById(id);

        CommentUpdateVo vo = new CommentUpdateVo();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }

    @Override
    public PageResult<CommentAdminVo> getAdminCommentPage(QueryAdminCommentDto dto) {
        int pageNum = PageUtil.pageNum(dto.getPage());
        int pageSize = PageUtil.pageSize(dto.getSize());
        Page<Comment> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getStatus()), Comment::getStatus, dto.getStatus())
                .eq(dto.getArticleId() != null, Comment::getArticleId, dto.getArticleId())
                .eq(dto.getUserId() != null, Comment::getUserId, dto.getUserId())
                .orderByDesc(Comment::getCreatedAt);

        this.page(page, wrapper);

        List<Comment> comments = page.getRecords();
        if (comments.isEmpty()) {
            return PageUtil.build(page, List.of());
        }

        // 批量查询文章，避免 N+1 问题
        Set<Long> articleIds = comments.stream()
                .map(Comment::getArticleId)
                .collect(Collectors.toSet());

        Map<Long, Article> articleMap = articleService.listByIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        List<CommentMyVo> voList = comments.stream().map(comment -> {
            CommentMyVo vo = new CommentMyVo();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setStatus(comment.getStatus());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setUpdatedAt(comment.getUpdatedAt());

            Article article = articleMap.get(comment.getArticleId());
            if (article != null) {
                vo.setArticle(Map.of("id", article.getId(), "title", article.getTitle()));
            }
            return vo;
        }).toList();

        return PageUtil.build(page, voList);
    }

    @Override
    public CommentStatusVo approveComment(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        comment.setStatus(CommentStatus.APPROVED.getValue());
        this.updateById(comment);

        CommentStatusVo vo = new CommentStatusVo();
        vo.setId(comment.getId());
        vo.setStatus(comment.getStatus());
        return vo;
    }

    @Override
    public CommentStatusVo rejectComment(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        comment.setStatus(CommentStatus.REJECTED.getValue());
        this.updateById(comment);

        CommentStatusVo vo = new CommentStatusVo();
        vo.setId(comment.getId());
        vo.setStatus(comment.getStatus());
        return vo;
    }

    @Override
    public void deleteCommentAdmin(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        this.removeById(id);
    }
}