package com.xblog.vo;

import lombok.Data;

@Data
public class MediaImageVo {
    private String url;
    private String type;
    private String sourceName;
    private Long sourceId;
    private boolean isOss;
}
