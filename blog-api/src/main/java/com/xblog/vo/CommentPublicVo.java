package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentPublicVo {
    private Long id;
    private String content;
    private AuthorVo user;
    private LocalDateTime createdAt;
}