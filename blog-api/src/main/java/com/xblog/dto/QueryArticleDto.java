package com.xblog.dto;

import lombok.Data;

@Data
public class QueryArticleDto {
    private Integer page = 1;
    private Integer size = 10;
    private Long categoryId;
    private Long tagId;

}
