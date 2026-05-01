package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryAdminVo {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
    private Long articleCount;
    private LocalDateTime createdAt;
}
