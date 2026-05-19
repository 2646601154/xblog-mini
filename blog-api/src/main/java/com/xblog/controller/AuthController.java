package com.xblog.controller;

import com.xblog.common.util.UserContext;
import com.xblog.dto.LoginParam;
import com.xblog.dto.RefreshTokenParam;
import com.xblog.dto.RegisterParam;
import com.xblog.entity.Result;
import com.xblog.entity.User;
import com.xblog.service.UserService;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.TokenVo;
import com.xblog.common.properties.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "认证接口", description = "登录、注册、刷新Token、登出接口")
public class AuthController {
    private final UserService userService;
    private final JwtProperties jwtProperties;

    public AuthController(UserService userService, JwtProperties jwtProperties) {
        this.userService = userService;
        this.jwtProperties = jwtProperties;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<TokenVo> login(@Valid @RequestBody LoginParam loginParam) {
        log.info("用户登录: {}", loginParam.getUsername());
        return Result.success(userService.login(loginParam));
    }

    @Operation(summary = "刷新Access Token")
    @PostMapping("/refresh")
    public Result<TokenVo> refresh(@Valid @RequestBody RefreshTokenParam param) {
        log.info("刷新Token请求");
        String result = userService.refreshAccessToken(param.getRefreshToken());
        String[] parts = result.split("\\|");
        TokenVo tokenVo = new TokenVo();
        tokenVo.setAccessToken(parts[0]);
        tokenVo.setRefreshToken(parts[1]);
        tokenVo.setExpiresIn(jwtProperties.getAccessExpiration() / 1000);
        return Result.success(tokenVo);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        log.info("用户登出, userId: {}", userId);
        userService.logout();
        return Result.success();
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<RegisterUserVo> register(@Valid @RequestBody RegisterParam registerParam) {
        log.info("用户注册: {}", registerParam.getUsername());
        return Result.success(userService.register(registerParam));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<User> me() {
        log.info("获取当前用户信息, userId: {}", UserContext.getUserId());
        return Result.success(userService.getLoginUser());
    }
}