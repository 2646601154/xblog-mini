package com.xblog.common.enums;

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
    EMAIL_EXISTS(2003, "邮箱已存在"),
    USER_DISABLE_FORBIDDEN(2009, "无法禁用管理员账号"),
    USER_DELETE_FORBIDDEN(2010, "无法删除管理员账号"),

    ARTICLE_NOT_FOUND(3000, "文章不存在"),
    ARTICLE_ALREADY_RECYCLED(3009, "文章已在回收站"),
    ARTICLE_TITLE_EMPTY(3002, "文章标题不能为空"),
    ARTICLE_CONTENT_EMPTY(3003, "文章内容不能为空"),
    ARTICLE_STATUS_INVALID(3005, "文章状态无效"),
    ARTICLE_CANNOT_EDIT_OTHERS(3006, "不能编辑他人的文章"),
    ARTICLE_CANNOT_DELETE_OTHERS(3007, "不能删除他人的文章"),
    ARTICLE_IN_RECYCLE_CANNOT_PUBLISH(3010, "文章在回收站，无法发布"),
    ARTICLE_NOT_IN_RECYCLE_CANNOT_RESTORE(3011, "文章不在回收站，无法恢复"),

    COMMENT_NOT_FOUND(4000, "评论不存在"),

    CATEGORY_NOT_FOUND(5000, "分类不存在"),
    CATEGORY_HAS_ARTICLES(5001, "分类下存在文章，无法删除"),
    CATEGORY_NAME_EMPTY(5002, "分类名称不能为空"),
    CATEGORY_NAME_EXISTS(5003, "分类名称已存在"),
    CATEGORY_SLUG_EXISTS(5004, "分类slug已存在"),
    CATEGORY_NAME_FORMAT_ERROR(5005, "分类名称格式错误"),
    CATEGORY_SLUG_FORMAT_ERROR(5006, "分类slug格式错误"),

    TAG_NOT_FOUND(6000, "标签不存在"),

    SYSTEM_ERROR(9000, "系统错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
