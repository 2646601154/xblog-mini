package com.xblog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户详情 / 创建 / 更新响应 VO。
 *
 * <p>本 VO 是字段白名单载体——<strong>所有字段在
 * {@code UserServiceImpl.getUserDetail/createUser/updateUser} 的 set 调用列表中显式控制</strong>。</p>
 *
 * <p>安全约束：</p>
 * <ul>
 *   <li>{@code password} 永不外泄（即使 {@code User} 实体上有该字段）</li>
 *   <li>{@code username} 保留：admin 编辑用户时需回填用户名</li>
 * </ul>
 */
@Data
public class UserDetailVo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
