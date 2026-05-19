package com.xblog.controller;

import com.xblog.dto.UpdatePasswordParam;
import com.xblog.dto.UpdateProfileParam;
import com.xblog.entity.Result;
import com.xblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@Tag(name = "用户接口", description = "普通用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileParam param) {
        log.info("修改个人资料");
        userService.updateProfile(param);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordParam param) {
        log.info("修改密码");
        userService.updatePassword(param);
        return Result.success();
    }
}
