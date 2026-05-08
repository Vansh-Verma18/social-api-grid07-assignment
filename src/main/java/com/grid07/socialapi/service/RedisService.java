package com.grid07.socialapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Atomically increment a Redis key and return the new value
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    /**
     * Atomically increment a Redis key by a specific delta
     */
    public Long incrementBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    /**
     * Check if a key exists in Redis
     */
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * Set a key with TTL (Time-To-Live)
     */
    public void setWithExpiry(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * Get value from Redis
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Push a value to a Redis list (right push)
     */
    public void pushToList(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }
    
    /**
     * Get all values from a Redis list
     */
    public List<Object> getList(String key) {
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return List.of();
        }
        return redisTemplate.opsForList().range(key, 0, -1);
    }
    
    /**
     * Delete a key from Redis
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * Get all keys matching a pattern
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    /**
     * Get the size of a list
     */
    public Long getListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }
}
