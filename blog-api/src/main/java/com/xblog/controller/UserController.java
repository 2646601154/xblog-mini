package com.xblog.controller;

import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.UserService;
import com.xblog.vo.UserVo;
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
    public Result<PageResult<UserVo>> queryUserList(@ModelAttribute QueryUserDto queryUserDto) {
        PageResult<UserVo> pageResult = userService.getUserPage(queryUserDto);
        return Result.success(pageResult);
    }
}
