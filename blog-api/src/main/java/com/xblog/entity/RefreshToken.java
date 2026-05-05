package com.xblog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Refresh Token 存储对象
 * <p>
 * 存储在 Redis 中，作为 Refresh Token 的值。
 * 包含用户信息，用于验证 Refresh Token 有效性后生成新的 Access Token。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}