package com.xblog.dto;

import lombok.Data;

@Data
public class QueryAdminCommentDto {
    private Integer page = 1;
    private Integer size = 10;
    private String status;
    private Long articleId;
    private Long userId;
}