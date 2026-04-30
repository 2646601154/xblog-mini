package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.ResultCode;
import com.xblog.common.util.PageUtil;
import com.xblog.dto.QueryUserDto;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.exception.BusinessException;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import com.xblog.vo.UserStatusVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public PageResult<User> getUserPage(QueryUserDto dto) {
        int pageNum = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getSize() != null ? dto.getSize() : 10;
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getRole()), User::getRole, dto.getRole())
                .eq(StringUtils.hasText(dto.getStatus()), User::getStatus, dto.getStatus());

        this.page(page, wrapper);
        return PageUtil.build(page);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setId(id);
        this.updateById(user);
        return this.getById(id);
    }

    @Override
    public UserStatusVo disableUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException(ResultCode.USER_DISABLE_FORBIDDEN);
        }
        user.setStatus("disabled");
        this.updateById(user);
        return new UserStatusVo(user.getId(), user.getStatus());
    }

    @Override
    public UserStatusVo enableUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus("normal");
        this.updateById(user);
        return new UserStatusVo(user.getId(), user.getStatus());
    }

    @Override
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException(ResultCode.USER_DELETE_FORBIDDEN);
        }
        this.removeById(id);
    }
}
