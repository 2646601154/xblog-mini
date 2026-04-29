package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.util.PageUtil;
import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public PageResult<User> getUserPage(QueryUserDto dto) {
        //默认page和pagesize
        int pageNum = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getSize() != null ? dto.getSize() : 10;
        Page<User> page = new Page<>(pageNum, pageSize);

        //构建wapper
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getRole()), User::getRole, dto.getRole())
                .eq(StringUtils.hasText(dto.getStatus()), User::getStatus, dto.getStatus());

        //查询
        this.page(page, wrapper);
        return PageUtil.build(page);
    }
}
