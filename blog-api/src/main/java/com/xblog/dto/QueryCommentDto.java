package com.xblog.dto;

import lombok.Data;

@Data
public class QueryCommentDto {
    private Long articleId;
    private Integer page = 1;
    private Integer size = 10;
}