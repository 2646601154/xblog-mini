package com.xblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xblog.entity.Config;
import com.xblog.dto.ConfigUpdateParam;
import com.xblog.vo.ConfigKeyValueVo;
import com.xblog.vo.ConfigVo;
import com.xblog.vo.PublicConfigVo;

import java.util.List;

public interface ConfigService extends IService<Config> {

    PublicConfigVo getPublicConfig();

    List<ConfigVo> getAllConfigs();

    List<ConfigKeyValueVo> updateConfigs(List<ConfigUpdateParam> configs);

    ConfigVo getConfigByKey(String key);
}