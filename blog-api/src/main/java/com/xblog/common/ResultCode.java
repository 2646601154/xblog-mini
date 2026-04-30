package com.xblog.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证 / Token 失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    AUTH_LOGIN_FAILED(1000, "登录失败"),
    AUTH_TOKEN_EXPIRED(1001, "Token 已过期"),
    AUTH_TOKEN_INVALID(1002, "Token 无效"),

    USER_NOT_FOUND(2000, "用户不存在"),
    USERNAME_EXISTS(2001, "用户名已存在"),
    USER_DISABLED(2002, "用户已被禁用"),
    USER_DISABLE_FORBIDDEN(2009, "无法禁用管理员账号"),
    USER_DELETE_FORBIDDEN(2010, "无法删除管理员账号"),

    ARTICLE_NOT_FOUND(3000, "文章不存在"),

    COMMENT_NOT_FOUND(4000, "评论不存在"),

    CATEGORY_NOT_FOUND(5000, "分类不存在"),
    CATEGORY_HAS_ARTICLES(5001, "分类下存在文章，无法删除"),

    TAG_NOT_FOUND(6000, "标签不存在"),

    SYSTEM_ERROR(9000, "系统错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
