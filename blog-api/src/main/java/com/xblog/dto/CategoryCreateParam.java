package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateParam {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 20, message = "分类名称长度必须为2-20字符")
    private String name;

    @NotBlank(message = "分类slug不能为空")
    @Size(min = 1, max = 50, message = "分类slug长度必须为1-50字符")
    private String slug;

    @Size(max = 200, message = "分类描述最多200字符")
    private String description;

    private Integer sortOrder;
}
