package com.xblog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserParam {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{2,19}$", message = "用户名需3-20位，字母开头")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码最少6位")
    private String password;

    @Size(max = 50, message = "昵称最多50字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String role = "user";
}
