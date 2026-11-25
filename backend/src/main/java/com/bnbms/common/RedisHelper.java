package com.bnbms.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 安全操作包装：Redis 不可用时自动降级，返回 null 或 false，不抛异常
 * 仅用于缓存等非关键路径；Token 存储等关键路径仍需 Redis 可用
 */
@Slf4j
@Component
public class RedisHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis GET 失败 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis SET 失败 key={}: {}", key, e.getMessage());
        }
    }

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("Redis SET 失败 key={}: {}", key, e.getMessage());
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis DEL 失败 key={}: {}", key, e.getMessage());
            return false;
        }
    }

    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis DEL 失败 keys={}: {}", keys, e.getMessage());
            return 0L;
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis EXPIRE 失败 key={}: {}", key, e.getMessage());
            return false;
        }
    }

    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("Redis SETNX 失败 key={}: {}", key, e.getMessage());
            // Redis 不可用时跳过分布式锁，单机直接放行
            return true;
        }
    }
}
