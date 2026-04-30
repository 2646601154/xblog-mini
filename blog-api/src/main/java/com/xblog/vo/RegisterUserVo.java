package com.xblog.vo;

import lombok.Data;

@Data
public class RegisterUserVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
}
