package com.xblog.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    BAD_REQUEST(400, "请求参数错误"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    AUTH_LOGIN_FAILED(1000, "登录失败"),
    AUTH_TOKEN_EXPIRED(1001, "Token 已过期"),
    AUTH_TOKEN_INVALID(1002, "Token 无效"),
    AUTH_REFRESH_TOKEN_INVALID(1003, "Refresh Token 无效或已过期"),

    USER_NOT_FOUND(2000, "用户不存在"),
    USERNAME_EXISTS(2001, "用户名已存在"),
    USER_DISABLED(2002, "用户已被禁用"),
    EMAIL_EXISTS(2003, "邮箱已存在"),
    OLD_PASSWORD_INCORRECT(2004, "原密码错误"),
    EMAIL_ALREADY_USED_BY_OTHERS(2005, "邮箱已被其他用户使用"),
    // 以下错误码为脱敏文案：仅服务端日志可读具体原因，客户端仅返回"操作被拒绝"
    // 详细 reason 必须在 throw 处用 log.warn 记录（带 userId/targetId 等上下文）
    USER_MODIFY_SELF_FORBIDDEN(2006, "操作被拒绝", true),
    USER_MODIFY_ADMIN_FORBIDDEN(2007, "操作被拒绝", true),
    USER_DISABLE_FORBIDDEN(2009, "操作被拒绝", true),
    USER_DELETE_FORBIDDEN(2010, "操作被拒绝", true),
    ARTICLE_NOT_FOUND(3000, "文章不存在"),
    ARTICLE_ALREADY_RECYCLED(3009, "文章已在回收站"),
    ARTICLE_STATUS_INVALID(3005, "文章状态无效"),
    // 跨用户写操作的拒绝原因脱敏
    ARTICLE_CANNOT_EDIT_OTHERS(3006, "操作被拒绝", true),
    ARTICLE_CANNOT_DELETE_OTHERS(3007, "操作被拒绝", true),
    ARTICLE_IN_RECYCLE_CANNOT_PUBLISH(3010, "文章在回收站，无法发布"),
    ARTICLE_NOT_IN_RECYCLE_CANNOT_RESTORE(3011, "文章不在回收站，无法恢复"),

    COMMENT_NOT_FOUND(4000, "评论不存在"),
    // 跨用户写操作的拒绝原因脱敏
    COMMENT_CANNOT_EDIT_OTHERS(4004, "操作被拒绝", true),
    COMMENT_CANNOT_DELETE_OTHERS(4005, "操作被拒绝", true),
    COMMENT_APPROVED_CANNOT_DELETE(4009, "已审核通过的评论无法删除"),
    CATEGORY_NOT_FOUND(5000, "分类不存在"),
    CATEGORY_HAS_ARTICLES(5001, "分类下存在文章，无法删除"),
    CATEGORY_NAME_EXISTS(5003, "分类名称已存在"),
    CATEGORY_SLUG_EXISTS(5004, "分类slug已存在"),

    TAG_NOT_FOUND(6000, "标签不存在"),
    TAG_NAME_EXISTS(6002, "标签名称已存在"),
    TAG_SLUG_EXISTS(6003, "标签slug已存在");

    private final int code;
    private final String message;
    /**
     * 标记该错误码是否含业务规则泄露。
     *
     * <p>{@code true} 时：</p>
     * <ul>
     *   <li>{@link com.xblog.handler.GlobalExceptionHandler} 强制使用 {@link #message}（generic）
     *       返回给客户端，忽略 throw 处传入的自定义 message</li>
     *   <li>详细原因必须在 throw 处用 {@code log.warn} 记录（带 userId/targetId 等上下文）</li>
     * </ul>
     */
    private final boolean sensitive;

    ResultCode(int code, String message) {
        this(code, message, false);
    }

    ResultCode(int code, String message, boolean sensitive) {
        this.code = code;
        this.message = message;
        this.sensitive = sensitive;
    }

}