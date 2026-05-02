package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticlePublishVo {
    private Long id;
    private String status;
    private LocalDateTime publishedAt;
}