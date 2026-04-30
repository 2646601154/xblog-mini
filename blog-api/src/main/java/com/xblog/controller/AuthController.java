package com.xblog.controller;

import com.xblog.dto.LoginParam;
import com.xblog.entity.Result;
import com.xblog.service.UserService;
import com.xblog.vo.LoginVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginParam loginParam) {
        return Result.success(userService.login(loginParam));
    }
}
