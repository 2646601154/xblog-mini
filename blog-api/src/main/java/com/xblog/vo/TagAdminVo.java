package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagAdminVo {
    private Long id;
    private String name;
    private String slug;
    private Long articleCount;
    private LocalDateTime createdAt;
}