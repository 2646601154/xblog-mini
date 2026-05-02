package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVo {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private CategoryPublicVo category;
    private AuthorVo author;
    private List<TagVo> tags;
    private String status;
    private Integer viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
