package com.xblog.vo;

import lombok.Data;

@Data
public class ConfigKeyValueVo {
    private String configKey;
    private String configValue;

    public ConfigKeyValueVo(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }
}