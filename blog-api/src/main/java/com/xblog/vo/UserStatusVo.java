package com.xblog.vo;

import lombok.Data;

@Data
public class UserStatusVo {
    private Long id;
    private String status;

    public UserStatusVo(Long id, String status) {
        this.id = id;
        this.status = status;
    }
}