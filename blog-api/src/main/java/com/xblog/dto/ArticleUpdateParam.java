package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleUpdateParam {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "标题最多200字符")
    private String title;

    @Size(max = 500, message = "摘要最多500字符")
    private String summary;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String coverImage;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private List<Long> tagIds;

    private String status;
}
