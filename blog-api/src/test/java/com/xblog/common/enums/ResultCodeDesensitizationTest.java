package com.xblog.common.enums;

import com.xblog.common.exception.BusinessException;
import com.xblog.entity.Result;
import com.xblog.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务错误码文案脱敏契约测试。
 *
 * <p>三层防御回归：</p>
 * <ol>
 *   <li>{@link ResultCode} 的 sensitive 文案必须恒为 "操作被拒绝"</li>
 *   <li>{@link GlobalExceptionHandler} 在 sensitive=true 时强制 generic message，
 *       即便 throw 处传了具体业务原因也被覆盖</li>
 *   <li>客户端永远拿不到包含业务关键词的 message</li>
 * </ol>
 */
class ResultCodeDesensitizationTest {

    private static final Set<ResultCode> MUST_BE_GENERIC = Set.of(
            ResultCode.USER_MODIFY_SELF_FORBIDDEN,
            ResultCode.USER_MODIFY_ADMIN_FORBIDDEN,
            ResultCode.USER_DISABLE_FORBIDDEN,
            ResultCode.USER_DELETE_FORBIDDEN,
            ResultCode.ARTICLE_CANNOT_EDIT_OTHERS,
            ResultCode.ARTICLE_CANNOT_DELETE_OTHERS,
            ResultCode.COMMENT_CANNOT_EDIT_OTHERS,
            ResultCode.COMMENT_CANNOT_DELETE_OTHERS
    );

    private static final String[] LEAK_KEYWORDS = {
            "管理员", "自己", "他人", "作者", "admin", "self", "other", "owner", "role"
    };

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ============ L1: 枚举层契约 ============

    @Test
    @DisplayName("L1: 8 个敏感错误码的 message 恒为 '操作被拒绝'")
    void l1_sensitiveCodesHaveGenericMessage() {
        for (ResultCode code : MUST_BE_GENERIC) {
            assertEquals("操作被拒绝", code.getMessage(),
                    () -> code.name() + " 的 message 必须是 '操作被拒绝'，当前是 '"
                            + code.getMessage() + "'");
        }
    }

    @Test
    @DisplayName("L1: 8 个敏感错误码的 message 不含业务关键词")
    void l1_sensitiveMessagesDontLeakKeywords() {
        for (ResultCode code : MUST_BE_GENERIC) {
            String msg = code.getMessage();
            for (String keyword : LEAK_KEYWORDS) {
                assertFalse(msg.contains(keyword),
                        () -> code.name() + " message '" + msg
                                + "' 不应包含业务关键词 '" + keyword + "'");
            }
        }
    }

    @Test
    @DisplayName("L1: 8 个敏感错误码标记了 sensitive=true")
    void l1_sensitiveCodesFlagged() {
        for (ResultCode code : MUST_BE_GENERIC) {
            assertTrue(code.isSensitive(),
                    () -> code.name() + " 必须标记 sensitive=true");
        }
    }

    @Test
    @DisplayName("L1: 8 个敏感错误码的 code 编号唯一（保证服务端日志可定位）")
    void l1_sensitiveCodesKeepUniqueCodes() {
        Set<Integer> codes = new HashSet<>();
        for (ResultCode code : MUST_BE_GENERIC) {
            assertTrue(codes.add(code.getCode()),
                    () -> "code 编号重复: " + code.getCode() + " -> " + code.name());
        }
    }

    // ============ L2: 处理器层契约（关键）============

    @Test
    @DisplayName("L2: Handler 在 sensitive=true 时强制使用 generic message（忽略 throw 处的自定义 message）")
    void l2_handlerForcesGenericMessageForSensitiveCodes() {
        // 即便 throw 时传了具体的、含业务关键词的 message，handler 必须覆盖
        for (ResultCode code : MUST_BE_GENERIC) {
            String dangerousMessage = "详细原因: targetUserId=1, role=admin, 自己的账号被检测";
            BusinessException ex = new BusinessException(code, dangerousMessage);

            Result<Void> result = handler.handleBusinessException(ex);

            assertAll(code.name(),
                    () -> assertEquals(code.getCode(), result.getCode()),
                    () -> assertEquals("操作被拒绝", result.getMessage(),
                            () -> code.name() + " 的 sensitive message 必须是 generic，"
                                    + "当前 handler 返回了 '" + result.getMessage() + "'，"
                                    + "说明 handler 没有强制使用 enum.getMessage()"),
                    () -> assertFalse(result.getMessage().contains("admin"),
                            () -> code.name() + " 不应泄露 'admin' 关键词"),
                    () -> assertFalse(result.getMessage().contains("自己"),
                            () -> code.name() + " 不应泄露 '自己' 关键词")
            );
        }
    }

    @Test
    @DisplayName("L2: Handler 对非 sensitive 错误码保留自定义 message（向后兼容）")
    void l2_handlerKeepsCustomMessageForNonSensitiveCodes() {
        // 登录场景使用自定义 message（"用户名或密码错误"），必须保持透出
        BusinessException ex = new BusinessException(
                ResultCode.AUTH_LOGIN_FAILED, "用户名或密码错误");
        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals("用户名或密码错误", result.getMessage(),
                "非 sensitive 错误码应透传自定义 message");
    }

    @Test
    @DisplayName("L2: Handler 对非 sensitive 错误码且无自定义 message 时使用 enum message")
    void l2_handlerUsesEnumMessageWhenNoCustomProvided() {
        BusinessException ex = new BusinessException(ResultCode.USER_NOT_FOUND);
        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals("用户不存在", result.getMessage());
    }

    // ============ L3: 端到端契约（模拟真实攻击者调用）============

    @Test
    @DisplayName("L3: 模拟攻击者：注入敏感错误码 + 自定义 message 仍无法获取业务情报")
    void l3_e2e_attackerCannotExtractBusinessRules() {
        // 攻击者注入：试图通过自定义 message 反推后端逻辑
        for (ResultCode code : MUST_BE_GENERIC) {
            String attemptedLeak = "user role=admin cannot be modified by self attempt by userId=2";
            BusinessException ex = new BusinessException(code, attemptedLeak);

            Result<Void> result = handler.handleBusinessException(ex);

            assertNotNull(result);
            assertEquals("操作被拒绝", result.getMessage(),
                    "攻击者拿到的必须是 generic '操作被拒绝'");
            // 验证所有 LEAK_KEYWORDS 都没出现
            for (String keyword : LEAK_KEYWORDS) {
                assertFalse(result.getMessage().contains(keyword),
                        () -> "result.message 不应包含关键词 '" + keyword
                                + "': " + result.getMessage());
            }
        }
    }
}
