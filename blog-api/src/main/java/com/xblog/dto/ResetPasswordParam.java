package com.xblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordParam {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码最少6位")
    private String newPassword;
}
