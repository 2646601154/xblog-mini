package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryPublicVo {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
}
