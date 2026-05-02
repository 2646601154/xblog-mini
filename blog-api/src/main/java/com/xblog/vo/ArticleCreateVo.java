package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleCreateVo {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime createdAt;
}
