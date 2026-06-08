package com.xblog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员更新用户请求参数。
 * 仅暴露管理员可修改的字段。{@code id/username/password/createdAt/updatedAt}
 * 不可通过此 DTO 传递，防止越权写入。
 */
@Data
public class UpdateUserParam {

    @Size(min = 2, max = 50, message = "昵称需2-50字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 500, message = "头像URL最多500字符")
    private String avatar;

    @Pattern(regexp = "^(admin|user)$", message = "角色必须为 admin 或 user")
    private String role;

    @Pattern(regexp = "^(normal|disabled)$", message = "状态必须为 normal 或 disabled")
    private String status;
}
