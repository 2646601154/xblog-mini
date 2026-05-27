package com.xblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.common.util.RedisUtil;
import com.xblog.dto.ConfigUpdateParam;
import com.xblog.entity.Config;
import com.xblog.mapper.ConfigMapper;
import com.xblog.service.ConfigService;
import com.xblog.vo.ConfigKeyValueVo;
import com.xblog.vo.ConfigVo;
import com.xblog.vo.PublicConfigVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private static final String CACHE_KEY_PUBLIC_CONFIG = "config:public";
    private static final long TTL_PUBLIC_CONFIG_MINUTES = 10;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public PublicConfigVo getPublicConfig() {
        // 尝试读取缓存
        try {
            PublicConfigVo cached = redisUtil.get(CACHE_KEY_PUBLIC_CONFIG);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            // 缓存读取失败，继续查询数据库
        }

        // 查询数据库
        List<Config> configs = this.list();
        Map<String, String> configMap = new HashMap<>();
        for (Config config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }

        PublicConfigVo vo = new PublicConfigVo();
        vo.setIcpNumber(configMap.get("icp_number"));
        vo.setCopyright(configMap.get("copyright"));

        // 写入缓存
        try {
            redisUtil.set(CACHE_KEY_PUBLIC_CONFIG, vo, TTL_PUBLIC_CONFIG_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败，忽略
        }

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
                    new LambdaQueryWrapper<Config>()
                            .eq(Config::getConfigKey, param.getConfigKey())
            );
            if (config != null) {
                config.setConfigValue(param.getConfigValue());
                this.updateById(config);
            }
        }

        // 清除缓存
        try {
            redisUtil.delete(CACHE_KEY_PUBLIC_CONFIG);
        } catch (Exception e) {
            // 缓存清除失败，忽略
        }

        return configs.stream()
                .map(param -> new ConfigKeyValueVo(param.getConfigKey(), param.getConfigValue()))
                .toList();
    }

    @Override
    public ConfigVo getConfigByKey(String key) {
        Config config = this.getOne(
                new LambdaQueryWrapper<Config>()
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
