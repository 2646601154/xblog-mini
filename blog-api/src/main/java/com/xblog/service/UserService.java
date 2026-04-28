package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.vo.UserVo;

public interface UserService extends IService<User> {

    PageResult<UserVo> getUserPage(QueryUserDto queryUserDto);
}
