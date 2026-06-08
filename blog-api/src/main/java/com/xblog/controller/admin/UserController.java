package com.xblog.controller.admin;

import com.xblog.dto.CreateUserParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.ResetPasswordParam;
import com.xblog.dto.UpdateUserParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.Result;
import com.xblog.service.UserService;
import com.xblog.vo.UserDetailVo;
import com.xblog.vo.UserListVo;
import com.xblog.vo.UserStatusVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("adminUserController")
@RequestMapping("/v1/admin/users")
@Tag(name = "管理-用户接口", description = "管理员用户管理接口")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询用户列表")
    @GetMapping
    public Result<PageResult<UserListVo>> getUserList(@ModelAttribute QueryUserDto queryUserDto) {
        log.info("查询用户列表, page={}, size={}", queryUserDto.getPage(), queryUserDto.getSize());
        PageResult<UserListVo> pageresult = userService.getUserPage(queryUserDto);
        return Result.success(pageresult);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<UserDetailVo> createUser(@Valid @RequestBody CreateUserParam param) {
        log.info("创建用户, username={}", param.getUsername());
        return Result.success(userService.createUser(param));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVo> getUserDetail(@PathVariable Long id) {
        log.info("查询用户详情, id={}", id);
        return Result.success(userService.getUserDetail(id));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<UserDetailVo> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserParam param) {
        log.info("更新用户, id={}", id);
        return Result.success(userService.updateUser(id, param));
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

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordParam param) {
        log.info("重置用户密码, id={}", id);
        userService.resetPassword(id, param);
        return Result.success();
    }
}
