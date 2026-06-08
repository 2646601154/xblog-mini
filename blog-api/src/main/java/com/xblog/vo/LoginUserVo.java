package com.xblog.vo;

import lombok.Data;

/**
 * 当前登录用户响应 VO（{@code /v1/auth/me}）。
 *
 * <p>本 VO 是字段白名单载体——<strong>所有字段在
 * {@code UserServiceImpl.getLoginUserInfo} 的 set 调用列表中显式控制</strong>，
 * 不与 {@code User} 实体自动同步。新增/删除字段必须同时修改 service 方法。</p>
 *
 * <p>安全约束：</p>
 * <ul>
 *   <li>{@code password} 永不外泄</li>
 *   <li>{@code createdAt/updatedAt} 不外泄（无业务价值）</li>
 * </ul>
 */
@Data
public class LoginUserVo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String status;
    private String email;
}
