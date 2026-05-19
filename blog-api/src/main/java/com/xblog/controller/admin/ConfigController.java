package com.xblog.controller.admin;

import com.xblog.dto.UpdateConfigRequest;
import com.xblog.entity.Result;
import com.xblog.service.ConfigService;
import com.xblog.vo.ConfigKeyValueVo;
import com.xblog.vo.ConfigVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("adminConfigController")
@RequestMapping("/v1/admin/configs")
@Tag(name = "管理-配置接口", description = "管理员配置管理接口")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "获取所有配置")
    @GetMapping
    public Result<List<ConfigVo>> getAllConfigs() {
        log.info("获取所有配置");
        return Result.success(configService.getAllConfigs());
    }

    @Operation(summary = "更新配置")
    @PutMapping
    public Result<List<ConfigKeyValueVo>> updateConfigs(@Valid @RequestBody UpdateConfigRequest request) {
        log.info("更新配置: {}条", request.getConfigs().size());
        return Result.success(configService.updateConfigs(request.getConfigs()));
    }

    @Operation(summary = "获取单个配置")
    @GetMapping("/{key}")
    public Result<ConfigVo> getConfigByKey(@PathVariable String key) {
        log.info("获取单个配置: key={}", key);
        return Result.success(configService.getConfigByKey(key));
    }
}