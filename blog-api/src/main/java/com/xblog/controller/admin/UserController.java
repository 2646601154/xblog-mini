package com.xblog.controller.admin;

import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.vo.UserStatusVo;
import com.xblog.entity.Result;
import com.xblog.entity.User;
import com.xblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/admin/users")
@Tag(name = "管理-用户接口", description = "管理员用户管理接口")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "查询用户列表")
    @GetMapping
    public Result<PageResult<User>> getUserList(@ModelAttribute QueryUserDto queryUserDto) {
        log.info("查询用户列表, page={}, size={}", queryUserDto.getPage(), queryUserDto.getSize());
        PageResult<User> pageresult = userService.getUserPage(queryUserDto);
        return Result.success(pageresult);
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        log.info("查询用户详情, id={}", id);
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户, id={}", id);
        return Result.success(userService.updateUser(id, user));
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/{id}/disable")
    public Result<UserStatusVo> disableUser(@PathVariable Long id) {
        log.info("禁用用户, id={}", id);
        return Result.success(userService.disableUser(id));
    }

    @Operation(summary = "启用用户")
    @PutMapping("/{id}/enable")
    public Result<UserStatusVo> enableUser(@PathVariable Long id) {
        log.info("启用用户, id={}", id);
        return Result.success(userService.enableUser(id));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户, id={}", id);
        userService.deleteUser(id);
        return Result.success();
    }
}