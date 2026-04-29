package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;

public interface UserService extends IService<User> {

    PageResult<User> getUserPage(QueryUserDto queryUserDto);
}
