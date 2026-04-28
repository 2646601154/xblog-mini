package com.xblog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class QueryUserDto {
    @Min(1)
    private Integer page = 1;
    @Min(1)
    @Max(50)
    private Integer size = 10;
    private String role; //角色筛选 (admin/user)
    private String status; //状态筛选 (normal/disabled)
}
