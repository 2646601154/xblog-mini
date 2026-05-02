package com.xblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.entity.Config;
import com.xblog.mapper.ConfigMapper;
import com.xblog.service.ConfigService;
import com.xblog.dto.ConfigUpdateParam;
import com.xblog.vo.ConfigKeyValueVo;
import com.xblog.vo.ConfigVo;
import com.xblog.vo.PublicConfigVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private static final String CACHE_KEY = "config:public";
    private static final long CACHE_TTL = 3600L;

    @Override
    public PublicConfigVo getPublicConfig() {
        // TODO: Redis 缓存支持
        // 1. 先从 Redis 缓存读取，key = "config:public"
        // 2. 缓存命中则直接返回
        // 3. 缓存未命中则查询数据库，存入缓存（TTL 1小时）

        List<Config> configs = this.list();
        Map<String, String> configMap = new HashMap<>();
        for (Config config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }

        PublicConfigVo vo = new PublicConfigVo();
        vo.setSiteTitle(configMap.get("site_title"));
        vo.setSiteLogo(configMap.get("site_logo"));
        vo.setSiteDescription(configMap.get("site_description"));
        vo.setIcpNumber(configMap.get("icp_number"));
        vo.setCopyright(configMap.get("copyright"));
        return vo;
    }

    @Override
    public List<ConfigVo> getAllConfigs() {
        List<Config> configs = this.list();
        return configs.stream().map(config -> {
            ConfigVo vo = new ConfigVo();
            BeanUtils.copyProperties(config, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<ConfigKeyValueVo> updateConfigs(List<ConfigUpdateParam> configs) {
        for (ConfigUpdateParam param : configs) {
            Config config = this.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Config>()
                            .eq(Config::getConfigKey, param.getConfigKey())
            );
            if (config != null) {
                config.setConfigValue(param.getConfigValue());
                this.updateById(config);
            }
        }

        // TODO: 清除 Redis 缓存，key = "config:public"

        return configs.stream()
                .map(param -> new ConfigKeyValueVo(param.getConfigKey(), param.getConfigValue()))
                .toList();
    }

    @Override
    public ConfigVo getConfigByKey(String key) {
        Config config = this.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Config>()
                        .eq(Config::getConfigKey, key)
        );
        if (config == null) {
            return null;
        }
        ConfigVo vo = new ConfigVo();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}