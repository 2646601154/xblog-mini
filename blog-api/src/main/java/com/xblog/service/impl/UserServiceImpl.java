package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.ResultCode;
import com.xblog.common.UserContext;
import com.xblog.common.util.JwtUtil;
import com.xblog.common.util.PageUtil;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.User;
import com.xblog.common.exception.BusinessException;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import com.xblog.vo.LoginUserVo;
import com.xblog.vo.LoginVo;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.UserStatusVo;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public UserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVo login(LoginParam loginParam) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginParam.getUsername());
        User user = this.getOne(wrapper);

        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_LOGIN_FAILED, "用户名或密码错误");
        }

        // 3. 用户已禁用
        if ("disabled".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 密码校验
        if (!passwordEncoder.matches(loginParam.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.AUTH_LOGIN_FAILED, "用户名或密码错误");
        }

        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 组装响应
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtils.copyProperties(user, loginUserVo);

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUser(loginUserVo);
        return loginVo;
    }

    @Override
    public RegisterUserVo register(RegisterParam registerParam) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerParam.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已存在
        if (StringUtils.hasText(registerParam.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerParam.getEmail());
            if (this.count(emailWrapper) > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        // 3. 构建用户对象
        User user = new User();
        user.setUsername(registerParam.getUsername());
        user.setPassword(passwordEncoder.encode(registerParam.getPassword()));
        user.setNickname(registerParam.getNickname());
        user.setEmail(registerParam.getEmail());
        user.setRole("user");
        user.setStatus("normal");

        // 4. 保存到数据库
        this.save(user);

        // 5. 组装响应
        RegisterUserVo vo = new RegisterUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public User getLoginUser() {
        Long userId = UserContext.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

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
