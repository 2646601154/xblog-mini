package com.xblog.controller;

import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.entity.User;
import com.xblog.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/users")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    public Result<PageResult<User>> queryUserList(@ModelAttribute QueryUserDto queryUserDto) {
        PageResult<User> pageresult = userService.getUserPage(queryUserDto);
        return Result.success(pageresult);
    }
}
