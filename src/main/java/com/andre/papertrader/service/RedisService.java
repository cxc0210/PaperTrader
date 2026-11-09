package com.andre.papertrader.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisService {

    private static final DefaultRedisScript<Long> REPLACE_SESSION_SCRIPT = new DefaultRedisScript<>("""
            local previousSessionKey = redis.call('GET', KEYS[1])
            if previousSessionKey then
                redis.call('DEL', previousSessionKey)
            end
            redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[2])
            redis.call('SET', KEYS[1], KEYS[2], 'PX', ARGV[2])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> DELETE_SESSION_SCRIPT = new DefaultRedisScript<>("""
            redis.call('DEL', KEYS[1])
            if redis.call('GET', KEYS[2]) == KEYS[1] then
                redis.call('DEL', KEYS[2])
            end
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> REFRESH_SESSION_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                return 0
            end
            if redis.call('GET', KEYS[2]) ~= KEYS[1] then
                return 0
            end
            redis.call('PEXPIRE', KEYS[1], ARGV[2])
            redis.call('PEXPIRE', KEYS[2], ARGV[2])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean expire(String key, Duration timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public void replaceSession(String userSessionKey, String sessionKey, String userId, Duration timeout) {
        redisTemplate.execute(
                REPLACE_SESSION_SCRIPT,
                List.of(userSessionKey, sessionKey),
                userId,
                String.valueOf(timeout.toMillis())
        );
    }

    public void deleteSession(String sessionKey, String userSessionKey) {
        redisTemplate.execute(DELETE_SESSION_SCRIPT, List.of(sessionKey, userSessionKey));
    }

    public boolean refreshSession(String sessionKey, String userSessionKey, String userId, Duration timeout) {
        Long result = redisTemplate.execute(
                REFRESH_SESSION_SCRIPT,
                List.of(sessionKey, userSessionKey),
                userId,
                String.valueOf(timeout.toMillis())
        );
        return Long.valueOf(1).equals(result);
    }
}
