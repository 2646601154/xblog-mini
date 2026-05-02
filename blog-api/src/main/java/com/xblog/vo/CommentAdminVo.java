package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CommentAdminVo {
    private Long id;
    private Map<String, Object> article;
    private Map<String, Object> user;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}