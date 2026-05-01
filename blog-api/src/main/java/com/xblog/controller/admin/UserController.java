package com.xblog.controller.admin;

import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.vo.UserStatusVo;
import com.xblog.entity.Result;
import com.xblog.entity.User;
import com.xblog.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/admin/users")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    public Result<PageResult<User>> getUserList(@ModelAttribute QueryUserDto queryUserDto) {
        log.info("查询用户列表, page={}, size={}", queryUserDto.getPage(), queryUserDto.getSize());
        PageResult<User> pageresult = userService.getUserPage(queryUserDto);
        return Result.success(pageresult);
    }

    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        log.info("查询用户详情, id={}", id);
        return Result.success(userService.getById(id));
    }

    //更新用户
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户, id={}", id);
        return Result.success(userService.updateUser(id, user));
    }

    //禁用账户
    @PutMapping("/{id}/disable")
    public Result<UserStatusVo> disableUser(@PathVariable Long id) {
        log.info("禁用用户, id={}", id);
        return Result.success(userService.disableUser(id));
    }

    //启用用户
    @PutMapping("/{id}/enable")
    public Result<UserStatusVo> enableUser(@PathVariable Long id) {
        log.info("启用用户, id={}", id);
        return Result.success(userService.enableUser(id));
    }

    //删除用户
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户, id={}", id);
        userService.deleteUser(id);
        return Result.success();
    }
}
