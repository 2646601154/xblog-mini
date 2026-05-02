package com.xblog.controller;

import com.xblog.entity.Result;
import com.xblog.service.ConfigService;
import com.xblog.vo.PublicConfigVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Slf4j
@Tag(name = "配置接口", description = "公开配置相关接口")
public class ConfigController {

    @Resource
    private ConfigService configService;

    @Operation(summary = "获取公开配置")
    @GetMapping("/configs")
    public Result<PublicConfigVo> getPublicConfig() {
        return Result.success(configService.getPublicConfig());
    }
}