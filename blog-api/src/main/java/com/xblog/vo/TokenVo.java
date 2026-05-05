package com.xblog.vo;

import lombok.Data;

/**
 * Token 响应 VO
 * <p>
 * 包含 Access Token 和 Refresh Token，用于登录和刷新接口。
 * </p>
 */
@Data
public class TokenVo {
    /**
     * Access Token（JWT，短期有效）
     */
    private String accessToken;

    /**
     * Refresh Token（UUID，用于刷新 AT）
     */
    private String refreshToken;

    /**
     * Access Token 剩余过期时间（秒）
     */
    private long expiresIn;
}