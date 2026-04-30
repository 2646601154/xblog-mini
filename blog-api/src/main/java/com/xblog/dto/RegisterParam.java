package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterParam {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{2,19}$", message = "用户名需3-20位，字母开头")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码最少6位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 50, message = "昵称需2-50字符")
    private String nickname;

    private String email;
}
