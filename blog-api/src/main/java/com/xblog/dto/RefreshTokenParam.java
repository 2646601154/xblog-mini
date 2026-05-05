package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求参数
 * <p>
 * 前端调用 /v1/auth/refresh 时传递 refreshToken。
 * </p>
 */
@Data
public class RefreshTokenParam {
    /**
     * Refresh Token（UUID 字符串）
     */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}