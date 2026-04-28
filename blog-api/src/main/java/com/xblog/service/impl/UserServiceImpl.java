package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import com.xblog.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public PageResult<UserVo> getUserPage(QueryUserDto queryUserDto) {
            int pageNum = queryUserDto.getPage() != null ? queryUserDto.getPage() : 1;
        int pageSize = queryUserDto.getSize() != null ? queryUserDto.getSize() : 10;
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryUserDto.getRole())) {
            wrapper.eq("role", queryUserDto.getRole());
        }
        if (StringUtils.hasText(queryUserDto.getStatus())) {
            wrapper.eq("status", queryUserDto.getStatus());
        }
        Page<User> userPage = page(page, wrapper);
        List<UserVo> records = userPage.getRecords().stream().map(user -> {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            return userVo;
        }).toList();
        PageResult<UserVo> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(userPage.getTotal());
        result.setPage((int) userPage.getCurrent());
        result.setSize((int) userPage.getSize());
        return result;
    }
}
