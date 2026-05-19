package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.properties.JwtProperties;
import com.xblog.common.util.UserContext;
import com.xblog.common.util.JwtUtil;
import com.xblog.common.util.PageUtil;
import com.xblog.common.util.RedisUtil;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.dto.UpdatePasswordParam;
import com.xblog.dto.UpdateProfileParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.RefreshToken;
import com.xblog.entity.User;
import com.xblog.common.exception.BusinessException;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.TokenVo;
import com.xblog.vo.UserStatusVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(JwtUtil jwtUtil, RedisUtil redisUtil, JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public TokenVo login(LoginParam loginParam) {
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

        // 5. 生成 Access Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 生成 Refresh Token UUID 并存入 Redis
        String refreshTokenUuid = jwtUtil.generateRefreshToken();
        String rtKey = "refresh_token:" + user.getId() + ":" + refreshTokenUuid;
        RefreshToken rtValue = new RefreshToken(user.getId(), user.getUsername(), user.getRole(), LocalDateTime.now());
        redisUtil.set(rtKey, rtValue, 7, TimeUnit.DAYS);
        // 6.5 添加反向索引（uuid -> key），用于快速查找 RT key
        redisTemplate.opsForHash().put("rt_uuid_map", refreshTokenUuid, rtKey);

        // 7. 组装响应
        TokenVo tokenVo = new TokenVo();
        tokenVo.setAccessToken(accessToken);
        tokenVo.setRefreshToken(refreshTokenUuid);
        tokenVo.setExpiresIn(jwtProperties.getAccessExpiration() / 1000);
        return tokenVo;
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        // 查找该用户所有 RT 的 key
        Set<String> rtKeys = redisTemplate.keys("refresh_token:" + userId + ":*");
        if (rtKeys != null && !rtKeys.isEmpty()) {
            // 删除所有 RT
            redisTemplate.delete(rtKeys);
            // 从反向索引中删除这些 key 对应的 uuid
            for (String rtKey : rtKeys) {
                String uuid = rtKey.substring(rtKey.lastIndexOf(":") + 1);
                redisTemplate.opsForHash().delete("rt_uuid_map", uuid);
            }
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        // 1. 从反向索引中查找 RT 对应的完整 key
        String rtKey = (String) redisTemplate.opsForHash().get("rt_uuid_map", refreshToken);
        if (rtKey == null) {
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 2. 检查 RT 是否存在
        RefreshToken rtValue = redisUtil.get(rtKey);
        if (rtValue == null) {
            // 清理无效的索引
            redisTemplate.opsForHash().delete("rt_uuid_map", refreshToken);
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 3. 原子性删除旧 RT（Token Rotation，防竞态）
        Boolean deleted = redisTemplate.delete(rtKey);
        if (!Boolean.TRUE.equals(deleted)) {
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 4. 从反向索引中删除 uuid
        redisTemplate.opsForHash().delete("rt_uuid_map", refreshToken);

        // 5. 生成新 Access Token
        String newAccessToken = jwtUtil.generateAccessToken(rtValue.getUserId(), rtValue.getUsername(), rtValue.getRole());

        // 6. 生成新 Refresh Token 并存入 Redis
        String newRefreshTokenUuid = jwtUtil.generateRefreshToken();
        String newRtKey = "refresh_token:" + rtValue.getUserId() + ":" + newRefreshTokenUuid;
        RefreshToken newRtValue = new RefreshToken(rtValue.getUserId(), rtValue.getUsername(), rtValue.getRole(), LocalDateTime.now());
        redisUtil.set(newRtKey, newRtValue, 7, TimeUnit.DAYS);

        // 7. 更新反向索引
        redisTemplate.opsForHash().put("rt_uuid_map", newRefreshTokenUuid, newRtKey);

        // 8. 返回新 AT 和新 RT，用 | 分隔
        return newAccessToken + "|" + newRefreshTokenUuid;
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
    public void updateProfile(UpdateProfileParam param) {
        Long userId = UserContext.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验邮箱是否被其他用户使用
        if (StringUtils.hasText(param.getEmail()) && !param.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, param.getEmail());
            if (this.count(emailWrapper) > 0) {
                throw new BusinessException(ResultCode.EMAIL_ALREADY_USED_BY_OTHERS);
            }
        }

        if (StringUtils.hasText(param.getNickname())) {
            user.setNickname(param.getNickname());
        }
        if (StringUtils.hasText(param.getAvatar())) {
            user.setAvatar(param.getAvatar());
        }
        if (StringUtils.hasText(param.getEmail())) {
            user.setEmail(param.getEmail());
        }

        this.updateById(user);
    }

    @Override
    public void updatePassword(UpdatePasswordParam param) {
        Long userId = UserContext.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(param.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_INCORRECT);
        }

        // 新密码加密并保存
        user.setPassword(passwordEncoder.encode(param.getNewPassword()));
        this.updateById(user);
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
