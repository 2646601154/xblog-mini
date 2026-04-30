package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;

import com.xblog.vo.LoginVo;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.UserStatusVo;

public interface UserService extends IService<User> {

    LoginVo login(LoginParam loginParam);

    RegisterUserVo register(RegisterParam registerParam);

    User getLoginUser();

    PageResult<User> getUserPage(QueryUserDto queryUserDto);

    User updateUser(Long id, User user);

    UserStatusVo disableUser(Long id);

    UserStatusVo enableUser(Long id);

    void deleteUser(Long id);
}
