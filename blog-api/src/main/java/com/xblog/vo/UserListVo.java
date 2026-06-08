package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户列表响应 VO（{@code GET /v1/admin/users}）。
 *
 * <p>本 VO 是字段白名单载体——<strong>所有字段在
 * {@code UserServiceImpl.getUserPage} 的 stream map 块中显式控制</strong>。</p>
 *
 * <p>安全约束（列表刻意收窄）：</p>
 * <ul>
 *   <li>{@code password} 永不外泄</li>
 *   <li>{@code username} 不暴露（admin 无需批量看到用户名）</li>
 *   <li>{@code updatedAt} 不暴露（不泄露最近活跃时间）</li>
 * </ul>
 */
@Data
public class UserListVo {
    private Long id;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
