package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagUpdateParam {

    @NotBlank(message = "标签名称不能为空")
    @Size(min = 1, max = 20, message = "标签名称格式错误")
    private String name;

    @NotBlank(message = "slug不能为空")
    @Size(min = 1, max = 50, message = "标签slug格式错误")
    private String slug;
}