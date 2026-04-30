package com.xblog.controller;

import com.xblog.common.UserContext;
import com.xblog.dto.LoginParam;
import com.xblog.dto.RegisterParam;
import com.xblog.entity.Result;
import com.xblog.entity.User;
import com.xblog.service.UserService;
import com.xblog.vo.LoginVo;
import com.xblog.vo.RegisterUserVo;
import jakarta.annotation.Resource;
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
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 登录
     *
     * @param loginParam 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginParam loginParam) {
        log.info("用户登录: {}", loginParam.getUsername());
        return Result.success(userService.login(loginParam));
    }

    /**
     * 注册
     *
     * @param registerParam 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<RegisterUserVo> register(@Valid @RequestBody RegisterParam registerParam) {
        log.info("用户注册: {}", registerParam.getUsername());
        return Result.success(userService.register(registerParam));
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Result<User> me() {
        log.info("获取当前用户信息, userId: {}", UserContext.getUserId());
        return Result.success(userService.getLoginUser());
    }
}
