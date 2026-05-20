package com.xblog.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装常用的 Redis 操作，提供类型安全的缓存读写方法。
 * 所有方法均基于 RedisTemplate，支持 String、Object、List、Set 等常见类型。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // ============================= String ============================

    /**
     * 写入缓存（无过期时间）
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入缓存并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取缓存
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return true 删除成功
     */
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除
     *
     * @param keys 键集合
     * @return 删除数量
     */
    public long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 按模式删除（基于 KEYS 命令匹配）
     * <p>
     * 注意：KEYS 命令会阻塞 Redis，建议在缓存清理等低频场景使用。
     * 高频场景建议使用 SCAN 替代。
     * </p>
     *
     * @param pattern 匹配模式（如 "article:list:*"）
     * @return 删除数量
     */
    public long deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.isEmpty()) {
            return 0;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true 存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取剩余过期时间
     *
     * @param key 键
     * @return 剩余秒数，-1 表示永不过期，-2 表示不存在
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // ============================= Increment ============================

    /**
     * 自增（用于计数器）
     *
     * @param key   键
     * @param delta 增量
     * @return 自增后的值
     */
    public long increment(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /**
     * 自减
     *
     * @param key   键
     * @param delta 减量
     * @return 自减后的值
     */
    public long decrement(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0;
    }

    // ============================= Hash ============================

    /**
     * Hash 写入
     *
     * @param key     键
     * @param hashKey 哈希键
     * @param value   值
     */
    public void hSet(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash 读取
     *
     * @param key     键
     * @param hashKey 哈希键
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, Object hashKey) {
        return (T) redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash 删除字段
     *
     * @param key      键
     * @param hashKeys 哈希键数组
     * @return 删除数量
     */
    public long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 判断 Hash 中是否存在指定字段
     *
     * @param key     键
     * @param hashKey 哈希键
     * @return true 存在
     */
    public boolean hHasKey(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // ============================= List ============================

    /**
     * List 左推入（头部）
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public long lPush(String key, Object value) {
        Long result = redisTemplate.opsForList().leftPush(key, value);
        return result != null ? result : 0;
    }

    /**
     * List 右弹出（尾部）
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T rPop(String key) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    /**
     * List 范围查询
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引（-1 表示到最后）
     * @return 值列表
     */
    @SuppressWarnings("unchecked")
    public <T> java.util.List<T> lRange(String key, long start, long end) {
        return (java.util.List<T>) redisTemplate.opsForList().range(key, start, end);
    }

    // ============================= Set ============================

    /**
     * Set 添加成员
     *
     * @param key    键
     * @param values 成员值
     * @return 添加数量
     */
    public long sAdd(String key, Object... values) {
        Long result = redisTemplate.opsForSet().add(key, values);
        return result != null ? result : 0;
    }

    /**
     * Set 判断成员是否存在
     *
     * @param key   键
     * @param value 成员值
     * @return true 存在
     */
    public boolean sIsMember(String key, Object value) {
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Set 移除成员
     *
     * @param key    键
     * @param values 成员值
     * @return 移除数量
     */
    public long sRemove(String key, Object... values) {
        Long result = redisTemplate.opsForSet().remove(key, values);
        return result != null ? result : 0;
    }

    // ============================= Distributed Lock ============================

    /**
     * 尝试获取分布式锁（基于 setIfAbsent）
     *
     * @param key     锁键
     * @param value   锁值（建议使用 UUID）
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 获取成功
     */
    public boolean tryLock(String key, Object value, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放分布式锁（仅当值匹配时才删除，防止误删他人锁）
     *
     * @param key   锁键
     * @param value 锁值
     * @return true 释放成功
     */
    public boolean releaseLock(String key, Object value) {
        // 使用 Lua 脚本保证原子性：先比较值，匹配才删除
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                java.util.Collections.singletonList(key),
                value
        );
        return result > 0;
    }
}
