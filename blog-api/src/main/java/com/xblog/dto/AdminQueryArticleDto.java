package com.xblog.dto;

import lombok.Data;

@Data
public class AdminQueryArticleDto {
    private Integer page = 1;
    private Integer size = 10;
    private String status;
    private Long categoryId;
    private String title;
}
