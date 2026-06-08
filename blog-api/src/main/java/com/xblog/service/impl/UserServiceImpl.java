package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.enums.ResultCode;
import com.xblog.common.exception.BusinessException;
import com.xblog.common.properties.JwtProperties;
import com.xblog.common.util.JwtUtil;
import com.xblog.common.util.OssUtil;
import com.xblog.common.util.PageUtil;
import com.xblog.common.util.RedisUtil;
import com.xblog.common.util.UserContext;
import com.xblog.dto.CreateUserParam;
import com.xblog.dto.LoginParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.RegisterParam;
import com.xblog.dto.ResetPasswordParam;
import com.xblog.dto.UpdatePasswordParam;
import com.xblog.dto.UpdateProfileParam;
import com.xblog.dto.UpdateUserParam;
import com.xblog.entity.PageResult;
import com.xblog.entity.RefreshToken;
import com.xblog.entity.User;
import com.xblog.mapper.UserMapper;
import com.xblog.service.UserService;
import com.xblog.vo.LoginUserVo;
import com.xblog.vo.RegisterUserVo;
import com.xblog.vo.TokenVo;
import com.xblog.vo.UserDetailVo;
import com.xblog.vo.UserListVo;
import com.xblog.vo.UserStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现。
 *
 * <h3>响应字段白名单约定（重要）</h3>
 * <p>本类所有返回 {@code *Vo} 的方法均采用"手写 set"内联转换，
 * <strong>不直接返回 {@code User} 实体</strong>，防止敏感字段（{@code password} 等）外泄。</p>
 *
 * <p>{@code User} 实体包含 {@code password/createdAt/updatedAt} 等不应外泄的字段。
 * 任何新增的对外读接口必须：</p>
 * <ol>
 *   <li>自行 {@code new} 对应 VO</li>
 *   <li>逐个 {@code vo.setXxx(user.getXxx())} 拷贝白名单字段</li>
 *   <li>不得调用 {@code vo.setPassword(...)}（即使 User 实体有该字段）</li>
 * </ol>
 *
 * <p>安全审计可通过 {@code grep "vo\\.set" UserServiceImpl.java} 快速核查暴露字段集。</p>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OssUtil ossUtil;

    public UserServiceImpl(JwtUtil jwtUtil, RedisUtil redisUtil, JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate, OssUtil ossUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        this.ossUtil = ossUtil;
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
        redisTemplate.expire("rt_uuid_map", 7, TimeUnit.DAYS);
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
        Set<String> rtKeys = redisUtil.scanKeys("refresh_token:" + userId + ":*");
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
    public UserDetailVo createUser(CreateUserParam param) {
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

        // 字段白名单内联转换（见类级 Javadoc 约定）
        UserDetailVo vo = new UserDetailVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        log.info("创建用户成功, userId={}, username={}", user.getId(), user.getUsername());
        return vo;
    }
    @Override
    public LoginUserVo getLoginUserInfo() {
        Long userId = UserContext.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 字段白名单内联转换
        LoginUserVo vo = new LoginUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setEmail(user.getEmail());
        return vo;
    }

    @Override
    public UserDetailVo getUserDetail(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 字段白名单内联转换
        UserDetailVo vo = new UserDetailVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    @Override
    public PageResult<UserListVo> getUserPage(QueryUserDto dto) {
        int pageNum = PageUtil.pageNum(dto.getPage());
        int pageSize = PageUtil.pageSize(dto.getSize());
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getRole()), User::getRole, dto.getRole())
                .eq(StringUtils.hasText(dto.getStatus()), User::getStatus, dto.getStatus());

        this.page(page, wrapper);
        // 字段白名单内联转换（password/username/updatedAt 故意不拷贝）
        List<UserListVo> records = page.getRecords().stream()
                .map(user -> {
                    UserListVo vo = new UserListVo();
                    vo.setId(user.getId());
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                    vo.setEmail(user.getEmail());
                    vo.setRole(user.getRole());
                    vo.setStatus(user.getStatus());
                    vo.setCreatedAt(user.getCreatedAt());
                    return vo;
                })
                .toList();
        return PageUtil.build(page, records);
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
            // 清理旧头像（OSS 上的旧图片）
            String oldAvatar = user.getAvatar();
            if (StringUtils.hasText(oldAvatar) && !oldAvatar.equals(param.getAvatar())) {
                try {
                    ossUtil.deleteFile(oldAvatar);
                    log.debug("旧 OSS 头像已删除: {}", oldAvatar);
                } catch (Exception e) {
                    log.warn("删除旧 OSS 头像失败（非 OSS 图片将忽略）: {}", oldAvatar);
                }
            }
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
    public UserDetailVo updateUser(Long id, UpdateUserParam param) {
        Long currentUserId = UserContext.getUserId();
        log.info("管理员更新用户, targetUserId={}, currentUserId={}", id, currentUserId);

        User existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 业务规则 1：禁止修改自己（防管理员降级/禁用自己）
        if (id.equals(currentUserId)) {
            log.warn("管理员更新用户失败, 不能修改自己, userId={}", id);
            throw new BusinessException(ResultCode.USER_MODIFY_SELF_FORBIDDEN);
        }

        // 业务规则 2：禁止修改其他管理员
        if ("admin".equals(existing.getRole())) {
            log.warn("管理员更新用户失败, 不能修改管理员账号, targetUserId={}", id);
            throw new BusinessException(ResultCode.USER_MODIFY_ADMIN_FORBIDDEN);
        }

        // 字段白名单更新：仅当请求中明确传了非空值才覆盖
        if (StringUtils.hasText(param.getNickname())) {
            existing.setNickname(param.getNickname());
        }
        if (StringUtils.hasText(param.getEmail())) {
            if (!param.getEmail().equals(existing.getEmail())) {
                // 邮箱变更需校验唯一性
                LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
                emailWrapper.eq(User::getEmail, param.getEmail());
                if (this.count(emailWrapper) > 0) {
                    log.warn("管理员更新用户失败, 邮箱已被其他用户使用: {}", param.getEmail());
                    throw new BusinessException(ResultCode.EMAIL_ALREADY_USED_BY_OTHERS);
                }
            }
            existing.setEmail(param.getEmail());
        }
        if (StringUtils.hasText(param.getAvatar())) {
            // 清理旧头像（OSS 上的旧图片）
            String oldAvatar = existing.getAvatar();
            if (StringUtils.hasText(oldAvatar) && !oldAvatar.equals(param.getAvatar())) {
                try {
                    ossUtil.deleteFile(oldAvatar);
                } catch (Exception e) {
                    log.warn("删除旧 OSS 头像失败（非 OSS 图片将忽略）: {}", oldAvatar);
                }
            }
            existing.setAvatar(param.getAvatar());
        }
        if (StringUtils.hasText(param.getRole())) {
            existing.setRole(param.getRole());
        }
        if (StringUtils.hasText(param.getStatus())) {
            existing.setStatus(param.getStatus());
        }

        // username/password/createdAt/updatedAt 不接受通过此 DTO 写入
        // MyBatis-Plus 的 updateById 仅更新非 null 字段，但已通过 DTO 字段白名单隔离

        this.updateById(existing);
        log.info("管理员更新用户成功, targetUserId={}", id);
        // 字段白名单内联转换
        User updated = this.getById(id);
        UserDetailVo vo = new UserDetailVo();
        vo.setId(updated.getId());
        vo.setUsername(updated.getUsername());
        vo.setNickname(updated.getNickname());
        vo.setAvatar(updated.getAvatar());
        vo.setEmail(updated.getEmail());
        vo.setRole(updated.getRole());
        vo.setStatus(updated.getStatus());
        vo.setCreatedAt(updated.getCreatedAt());
        vo.setUpdatedAt(updated.getUpdatedAt());
        return vo;
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
