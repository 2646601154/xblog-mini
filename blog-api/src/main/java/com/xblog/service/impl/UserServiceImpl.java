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
import com.xblog.dto.CreateUserParam;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.dto.ResetPasswordParam;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
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
        log.info("用户登录, username={}", loginParam.getUsername());
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginParam.getUsername());
        User user = this.getOne(wrapper);

        // 2. 用户不存在
        if (user == null) {
            log.warn("用户登录失败, 用户名不存在: {}", loginParam.getUsername());
            throw new BusinessException(ResultCode.AUTH_LOGIN_FAILED, "用户名或密码错误");
        }

        // 3. 用户已禁用
        if ("disabled".equals(user.getStatus())) {
            log.warn("用户登录失败, 用户已禁用: {}", loginParam.getUsername());
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 密码校验
        if (!passwordEncoder.matches(loginParam.getPassword(), user.getPassword())) {
            log.warn("用户登录失败, 密码错误: {}", loginParam.getUsername());
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
        log.info("用户登录成功, userId={}, username={}", user.getId(), user.getUsername());
        return tokenVo;
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        log.info("用户登出, userId={}", userId);
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
            log.info("用户登出成功, 已清除 {} 个 Refresh Token, userId={}", rtKeys.size(), userId);
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        log.info("刷新 Access Token");
        // 1. 从反向索引中查找 RT 对应的完整 key
        String rtKey = (String) redisTemplate.opsForHash().get("rt_uuid_map", refreshToken);
        if (rtKey == null) {
            log.warn("刷新 Token 失败, Refresh Token 无效");
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 2. 检查 RT 是否存在
        RefreshToken rtValue = redisUtil.get(rtKey);
        if (rtValue == null) {
            // 清理无效的索引
            redisTemplate.opsForHash().delete("rt_uuid_map", refreshToken);
            log.warn("刷新 Token 失败, Refresh Token 不存在于 Redis");
            throw new BusinessException(ResultCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 3. 原子性删除旧 RT（Token Rotation，防竞态）
        Boolean deleted = redisTemplate.delete(rtKey);
        if (!Boolean.TRUE.equals(deleted)) {
            log.warn("刷新 Token 失败, 删除旧 RT 失败");
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
        log.info("刷新 Token 成功, userId={}", rtValue.getUserId());
        return newAccessToken + "|" + newRefreshTokenUuid;
    }

    @Override
    public RegisterUserVo register(RegisterParam registerParam) {
        log.info("用户注册, username={}, email={}", registerParam.getUsername(), registerParam.getEmail());
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerParam.getUsername());
        if (this.count(wrapper) > 0) {
            log.warn("用户注册失败, 用户名已存在: {}", registerParam.getUsername());
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已存在
        if (StringUtils.hasText(registerParam.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerParam.getEmail());
            if (this.count(emailWrapper) > 0) {
                log.warn("用户注册失败, 邮箱已存在: {}", registerParam.getEmail());
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
        log.info("用户注册成功, userId={}, username={}", user.getId(), user.getUsername());
        return vo;
    }

    @Override
    public User createUser(CreateUserParam param) {
        log.info("管理员创建用户, username={}, role={}", param.getUsername(), param.getRole());
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, param.getUsername());
        if (this.count(wrapper) > 0) {
            log.warn("创建用户失败, 用户名已存在: {}", param.getUsername());
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已存在
        if (StringUtils.hasText(param.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, param.getEmail());
            if (this.count(emailWrapper) > 0) {
                log.warn("创建用户失败, 邮箱已存在: {}", param.getEmail());
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        // 3. 构建用户对象
        User user = new User();
        user.setUsername(param.getUsername());
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        user.setNickname(StringUtils.hasText(param.getNickname()) ? param.getNickname() : param.getUsername());
        user.setEmail(param.getEmail());
        user.setRole(StringUtils.hasText(param.getRole()) ? param.getRole() : "user");
        user.setStatus("normal");

        // 4. 保存到数据库
        this.save(user);

        log.info("创建用户成功, userId={}, username={}", user.getId(), user.getUsername());
        return user;
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
        log.info("更新个人资料, userId={}", userId);
        User user = this.getById(userId);
        if (user == null) {
            log.warn("更新个人资料失败, 用户不存在, userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验邮箱是否被其他用户使用
        if (StringUtils.hasText(param.getEmail()) && !param.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, param.getEmail());
            if (this.count(emailWrapper) > 0) {
                log.warn("更新个人资料失败, 邮箱已被其他用户使用: {}", param.getEmail());
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
        log.info("更新个人资料成功, userId={}", userId);
    }

    @Override
    public void updatePassword(UpdatePasswordParam param) {
        Long userId = UserContext.getUserId();
        log.info("修改密码, userId={}", userId);
        User user = this.getById(userId);
        if (user == null) {
            log.warn("修改密码失败, 用户不存在, userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(param.getOldPassword(), user.getPassword())) {
            log.warn("修改密码失败, 旧密码错误, userId={}", userId);
            throw new BusinessException(ResultCode.OLD_PASSWORD_INCORRECT);
        }

        // 新密码加密并保存
        user.setPassword(passwordEncoder.encode(param.getNewPassword()));
        this.updateById(user);
        log.info("修改密码成功, userId={}", userId);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordParam param) {
        log.info("重置密码, userId={}", id);
        User user = this.getById(id);
        if (user == null) {
            log.warn("重置密码失败, 用户不存在, userId={}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // BCrypt 加密新密码
        user.setPassword(passwordEncoder.encode(param.getNewPassword()));
        this.updateById(user);
        log.info("重置密码成功, userId={}", id);
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
        log.info("禁用用户, targetUserId={}", id);
        User user = this.getById(id);
        if (user == null) {
            log.warn("禁用用户失败, 用户不存在, userId={}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if ("admin".equals(user.getRole())) {
            log.warn("禁用用户失败, 不能禁用管理员, userId={}", id);
            throw new BusinessException(ResultCode.USER_DISABLE_FORBIDDEN);
        }
        user.setStatus("disabled");
        this.updateById(user);
        log.info("禁用用户成功, userId={}", id);
        return new UserStatusVo(user.getId(), user.getStatus());
    }

    @Override
    public UserStatusVo enableUser(Long id) {
        log.info("启用用户, targetUserId={}", id);
        User user = this.getById(id);
        if (user == null) {
            log.warn("启用用户失败, 用户不存在, userId={}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus("normal");
        this.updateById(user);
        log.info("启用用户成功, userId={}", id);
        return new UserStatusVo(user.getId(), user.getStatus());
    }

    @Override
    public void deleteUser(Long id) {
        log.info("删除用户, targetUserId={}", id);
        User user = this.getById(id);
        if (user == null) {
            log.warn("删除用户失败, 用户不存在, userId={}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if ("admin".equals(user.getRole())) {
            log.warn("删除用户失败, 不能删除管理员, userId={}", id);
            throw new BusinessException(ResultCode.USER_DELETE_FORBIDDEN);
        }
        this.removeById(id);
        log.info("删除用户成功, userId={}", id);
    }
}
