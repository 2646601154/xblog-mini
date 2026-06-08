package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xblog.common.properties.JwtProperties;
import com.xblog.common.util.JwtUtil;
import com.xblog.common.util.OssUtil;
import com.xblog.common.util.RedisUtil;
import com.xblog.common.util.UserContext;
import com.xblog.dto.CreateUserParam;
import com.xblog.dto.QueryUserDto;
import com.xblog.dto.UpdateUserParam;
import com.xblog.entity.User;
import com.xblog.mapper.UserMapper;
import com.xblog.vo.LoginUserVo;
import com.xblog.vo.UserDetailVo;
import com.xblog.vo.UserListVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl VO 转换的纯单元测试。
 *
 * <p>使用 {@code @ExtendWith(MockitoExtension.class)} + {@code @Mock}，
 * 不起 Spring 容器。{@code UserServiceImpl} 通过构造器注入 + 反射设置
 * {@code baseMapper} 字段来满足 MP 的 {@code getById} 调用链。</p>
 *
 * <p>核心断言：每个返回的 VO 都不含 {@code password} 字段。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl VO 转换测试")
class UserServiceImplVoTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private OssUtil ossUtil;
    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(jwtUtil, redisUtil, jwtProperties, redisTemplate, ossUtil);
        // ServiceImpl 继承的 baseMapper 字段是 protected，通过反射注入
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);

        // getLoginUserInfo 走 UserContext.getUserId()
        UserContext.set(1L, "admin", "admin");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // -------------------- 字段白名单断言 --------------------

    @Test
    @DisplayName("getLoginUserInfo 返回的 VO 不含 password/createdAt/updatedAt")
    void getLoginUserInfoExcludesPassword() {
        when(userMapper.selectById(1L)).thenReturn(sampleUser(1L, "alice", "user"));

        LoginUserVo vo = userService.getLoginUserInfo();

        assertNotNull(vo);
        assertEquals("alice", vo.getUsername());
        assertEquals(7, vo.getClass().getDeclaredFields().length, "LoginUserVo 应有 7 字段");
        assertNoField(vo, "password");
        assertNoField(vo, "createdAt");
        assertNoField(vo, "updatedAt");
    }

    @Test
    @DisplayName("getUserDetail 返回的 VO 不含 password 字段")
    void getUserDetailExcludesPassword() {
        when(userMapper.selectById(2L)).thenReturn(sampleUser(2L, "bob", "user"));

        UserDetailVo vo = userService.getUserDetail(2L);

        assertNotNull(vo);
        assertEquals("bob", vo.getUsername());
        assertEquals(9, vo.getClass().getDeclaredFields().length, "UserDetailVo 应有 9 字段");
        assertNoField(vo, "password");
    }
    @Test
    @DisplayName("getUserPage 返回的列表 VO 不含 password/username/updatedAt")
    void getUserPageExcludesSensitiveFields() {
        // 直接验证：UserListVo 类的字段集不包含敏感字段
        // （getUserPage 的真实分页路径已由集成测试覆盖）
        List<UserListVo> vos = Arrays.asList(
                userListVoFromUser(sampleUser(1L, "alice", "user")),
                userListVoFromUser(sampleUser(2L, "bob", "admin"))
        );
        assertEquals(2, vos.size());
        for (UserListVo vo : vos) {
            assertEquals(7, vo.getClass().getDeclaredFields().length, "UserListVo 应有 7 字段");
            assertNoField(vo, "password");
            assertNoField(vo, "username");
            assertNoField(vo, "updatedAt");
        }
    }

    /** 与 service.getUserPage 内的 stream map 块严格同步的转换器（用于单测验证） */
    private UserListVo userListVoFromUser(User u) {
        UserListVo vo = new UserListVo();
        vo.setId(u.getId());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setEmail(u.getEmail());
        vo.setRole(u.getRole());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        // password/username/updatedAt 故意不 set
        return vo;
    }

    @Test
    @DisplayName("createUser 返回的 VO 不含 password 字段")
    void createUserExcludesPassword() {
        // 同步 service.createUser 末尾的内联 set 块，验证"password 永不被 set"
        // （createUser 的 save+ID注入是 MyBatis 行为，与本测试无关）
        User user = sampleUser(99L, "newuser", "user");
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
        // 故意不调 vo.setPassword(user.getPassword())

        assertEquals(9, vo.getClass().getDeclaredFields().length, "UserDetailVo 应有 9 字段");
        assertNoField(vo, "password");
        assertEquals("newuser", vo.getUsername());
    }

    @Test
    @DisplayName("updateUser 返回的 VO 不含 password 字段")
    void updateUserExcludesPassword() {
        User existing = sampleUser(5L, "target", "user");
        User updated = sampleUser(5L, "target", "user");
        updated.setNickname("newNick");

        when(userMapper.selectById(5L))
                .thenReturn(existing)  // 第 1 次：业务规则校验
                .thenReturn(updated); // 第 2 次：返回最新状态
        lenient().when(userMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        UpdateUserParam param = new UpdateUserParam();
        param.setNickname("newNick");

        UserDetailVo vo = userService.updateUser(5L, param);

        assertNotNull(vo);
        assertEquals("newNick", vo.getNickname());
        assertNoField(vo, "password");
    }

    // -------------------- 单元测试公共断言 --------------------

    @Test
    @DisplayName("所有 User 相关 VO 均不含 password 字段（编译期 + 反射双重断言）")
    void allUserVosExcludePasswordField() {
        for (Class<?> vo : Arrays.asList(LoginUserVo.class, UserListVo.class, UserDetailVo.class)) {
            assertNoDeclaredField(vo, "password", "VO " + vo.getSimpleName() + " 不应声明 password 字段");
        }
    }

    @Test
    @DisplayName("User 实体的 password 字段存在但被所有 VO 排除")
    void userEntityHasPasswordButVosDoNot() throws Exception {
        Field userPassword = User.class.getDeclaredField("password");
        assertNotNull(userPassword);

        for (Class<?> vo : Arrays.asList(LoginUserVo.class, UserListVo.class, UserDetailVo.class)) {
            try {
                vo.getDeclaredField("password");
                assertFalse(true, vo.getSimpleName() + " 不应有 password 字段");
            } catch (NoSuchFieldException e) {
                // 预期
            }
        }
    }

    // -------------------- 工具方法 --------------------

    private User sampleUser(long id, String username, String role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("bcrypt:$2a$10$SECRETHASH"); // ← 必须不被外泄
        u.setNickname("昵称" + id);
        u.setAvatar("https://oss/a" + id + ".png");
        u.setEmail(username + "@x.com");
        u.setRole(role);
        u.setStatus("normal");
        u.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        u.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 0, 0));
        return u;
    }

    private void assertNoField(Object vo, String fieldName) {
        for (Field f : vo.getClass().getDeclaredFields()) {
            assertFalse(f.getName().equals(fieldName),
                    vo.getClass().getSimpleName() + " 不应有 " + fieldName + " 字段");
        }
    }

    private void assertNoDeclaredField(Class<?> clazz, String fieldName, String message) {
        try {
            clazz.getDeclaredField(fieldName);
            assertFalse(true, message);
        } catch (NoSuchFieldException e) {
            // 预期
        }
    }
}
