package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.CreateUserParam;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.dto.UpdatePasswordParam;
import com.xblog.dto.UpdateProfileParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;

import com.xblog.vo.TokenVo;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.UserStatusVo;

public interface UserService extends IService<User> {

    TokenVo login(LoginParam loginParam);

    void logout();

    String refreshAccessToken(String refreshToken);

    RegisterUserVo register(RegisterParam registerParam);

    User createUser(CreateUserParam param);

    User getLoginUser();

    PageResult<User> getUserPage(QueryUserDto queryUserDto);

    User updateUser(Long id, User user);

    void updateProfile(UpdateProfileParam param);

    void updatePassword(UpdatePasswordParam param);

    UserStatusVo disableUser(Long id);

    UserStatusVo enableUser(Long id);

    void deleteUser(Long id);
}
