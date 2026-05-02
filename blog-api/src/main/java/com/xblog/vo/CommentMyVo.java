package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CommentMyVo {
    private Long id;
    private Map<String, Object> article;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}