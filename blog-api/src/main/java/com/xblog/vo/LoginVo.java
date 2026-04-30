package com.xblog.vo;

import lombok.Data;

@Data
public class LoginVo {
    private String token;
    private LoginUserVo user;
}
