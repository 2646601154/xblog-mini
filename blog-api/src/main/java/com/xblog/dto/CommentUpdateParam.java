package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentUpdateParam {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最多1000字符")
    private String content;
}