package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateParam {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;
}