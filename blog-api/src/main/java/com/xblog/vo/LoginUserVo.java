package com.xblog.vo;

import lombok.Data;

@Data
public class LoginUserVo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}
