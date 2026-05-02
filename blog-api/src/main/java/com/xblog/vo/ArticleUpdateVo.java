package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleUpdateVo {
    private Long id;
    private String title;
    private LocalDateTime updatedAt;
}
