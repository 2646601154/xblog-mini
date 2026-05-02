package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentCreatedVo {
    private Long id;
    private Long articleId;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}