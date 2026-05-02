package com.xblog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateConfigRequest {

    @NotEmpty(message = "配置列表不能为空")
    @Valid
    private List<ConfigUpdateParam> configs;
}