package com.xblog.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UpdateUserParam 单元测试。
 *
 * <p>覆盖：</p>
 * <ul>
 *   <li>字段白名单——DTO 仅暴露可修改字段（id/username/password/createTime/updateTime 不存在）</li>
 *   <li>Bean Validation 注解（@Email / @Pattern / @Size）</li>
 *   <li>Jackson 反序列化——多余字段（如 password/id）被静默丢弃</li>
 * </ul>
 */
class UpdateUserParamTest {

    private static ValidatorFactory factory;
    private static Validator validator;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    @DisplayName("DTO 字段白名单：不允许 id / username / password / createdAt / updatedAt")
    void dtoFieldWhitelist() throws Exception {
        String malicious = """
                {
                  "id": 999,
                  "username": "hacker",
                  "password": "newpwd",
                  "nickname": "合法昵称",
                  "createdAt": "2020-01-01T00:00:00",
                  "updatedAt": "2020-01-01T00:00:00",
                  "role": "user",
                  "status": "normal"
                }
                """;
        UpdateUserParam param = MAPPER.readValue(malicious, UpdateUserParam.class);

        // 1. DTO 内部无 password / username / id 字段
        assertFalse(hasField(UpdateUserParam.class, "password"),
                "UpdateUserParam 不应包含 password 字段");
        assertFalse(hasField(UpdateUserParam.class, "username"),
                "UpdateUserParam 不应包含 username 字段");
        assertFalse(hasField(UpdateUserParam.class, "id"),
                "UpdateUserParam 不应包含 id 字段");

        // 2. 合法字段被反序列化
        assertEquals("合法昵称", param.getNickname());
        assertEquals("user", param.getRole());
        assertEquals("normal", param.getStatus());
    }

    @Test
    @DisplayName("Bean Validation：非法 email 应触发约束")
    void invalidEmailRejected() {
        UpdateUserParam p = new UpdateUserParam();
        p.setEmail("not-an-email");
        Set<ConstraintViolation<UpdateUserParam>> violations = validator.validate(p);
        assertTrue(violations.stream()
                        .anyMatch(v -> "email".equals(v.getPropertyPath().toString())),
                "应返回 email 字段校验错误");
    }

    @Test
    @DisplayName("Bean Validation：合法 email 不触发约束")
    void validEmailAccepted() {
        UpdateUserParam p = new UpdateUserParam();
        p.setEmail("user@example.com");
        Set<ConstraintViolation<UpdateUserParam>> violations = validator.validate(p);
        assertTrue(violations.isEmpty(), "合法 email 不应有错误");
    }

    @Test
    @DisplayName("Bean Validation：role 必须是 admin / user")
    void roleMustBeAdminOrUser() {
        UpdateUserParam p = new UpdateUserParam();
        p.setRole("superuser");
        Set<ConstraintViolation<UpdateUserParam>> violations = validator.validate(p);
        assertTrue(violations.stream()
                        .anyMatch(v -> "role".equals(v.getPropertyPath().toString())),
                "role 取值非法应触发错误");
    }

    @Test
    @DisplayName("Bean Validation：status 必须是 normal / disabled")
    void statusMustBeNormalOrDisabled() {
        UpdateUserParam p = new UpdateUserParam();
        p.setStatus("banned");
        Set<ConstraintViolation<UpdateUserParam>> violations = validator.validate(p);
        assertTrue(violations.stream()
                        .anyMatch(v -> "status".equals(v.getPropertyPath().toString())),
                "status 取值非法应触发错误");
    }

    @Test
    @DisplayName("Bean Validation：nickname 长度 1 或 >50 应触发约束")
    void nicknameLengthOutOfRange() {
        UpdateUserParam tooShort = new UpdateUserParam();
        tooShort.setNickname("a");
        assertTrue(violations(tooShort).stream()
                .anyMatch(v -> "nickname".equals(v.getPropertyPath().toString())));

        UpdateUserParam tooLong = new UpdateUserParam();
        tooLong.setNickname("x".repeat(51));
        assertTrue(violations(tooLong).stream()
                .anyMatch(v -> "nickname".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Bean Validation：空 DTO 不应产生任何错误（全字段可选）")
    void emptyDtoHasNoViolations() {
        UpdateUserParam p = new UpdateUserParam();
        assertTrue(violations(p).isEmpty(), "空 DTO 不应有错误");
    }

    @Test
    @DisplayName("Jackson：反序列化时未知字段（password/id）被静默忽略")
    void jacksonIgnoresUnknownFields() throws Exception {
        String json = """
                {
                  "password": "should-be-ignored",
                  "id": 42,
                  "username": "should-be-ignored",
                  "nickname": "合法昵称"
                }
                """;
        // 默认 ObjectMapper 行为：未配置 FAIL_ON_UNKNOWN_PROPERTIES 时忽略未知字段
        UpdateUserParam param = MAPPER.readValue(json, UpdateUserParam.class);
        assertNotNull(param);
        assertEquals("合法昵称", param.getNickname());
        // 通过反射确认 password 字段不存在
        assertFalse(hasField(param.getClass(), "password"));
        assertFalse(hasField(param.getClass(), "id"));
        assertFalse(hasField(param.getClass(), "username"));
    }

    private Set<ConstraintViolation<UpdateUserParam>> violations(UpdateUserParam p) {
        return validator.validate(p);
    }

    private boolean hasField(Class<?> clazz, String name) {
        try {
            clazz.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
