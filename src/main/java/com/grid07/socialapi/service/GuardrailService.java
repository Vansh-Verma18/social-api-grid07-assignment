package com.grid07.socialapi.service;

import com.grid07.socialapi.exception.GuardrailViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardrailService {
    
    private final RedisService redisService;
    
    @Value("${app.redis.horizontal-cap:100}")
    private int horizontalCap;
    
    @Value("${app.redis.vertical-cap:20}")
    private int verticalCap;
    
    @Value("${app.redis.cooldown-minutes:10}")
    private int cooldownMinutes;
    
    /**
     * Check and enforce horizontal cap (max 100 bot replies per post)
     * Uses atomic Redis INCR to ensure thread-safety
     */
    public void checkHorizontalCap(Long postId) {
        String key = String.format("post:%d:bot_count", postId);
        Long currentCount = redisService.increment(key);
        
        log.debug("Post {} bot count: {}", postId, currentCount);
        
        if (currentCount > horizontalCap) {
            // Rollback the increment since we exceeded the cap
            redisService.incrementBy(key, -1);
            throw new GuardrailViolationException(
                String.format("Horizontal cap exceeded: Post %d already has %d bot replies", 
                    postId, horizontalCap)
            );
        }
    }
    
    /**
     * Check vertical cap (max depth level of 20)
     */
    public void checkVerticalCap(Integer depthLevel) {
        if (depthLevel > verticalCap) {
            throw new GuardrailViolationException(
                String.format("Vertical cap exceeded: Comment depth %d exceeds maximum of %d", 
                    depthLevel, verticalCap)
            );
        }
    }
    
    /**
     * Check and enforce cooldown cap (bot cannot interact with same human within 10 minutes)
     * Uses Redis key with TTL for automatic expiration
     */
    public void checkCooldownCap(Long botId, Long humanId) {
        String key = String.format("cooldown:bot_%d:human_%d", botId, humanId);
        
        if (Boolean.TRUE.equals(redisService.exists(key))) {
            throw new GuardrailViolationException(
                String.format("Cooldown active: Bot %d cannot interact with User %d yet", 
                    botId, humanId)
            );
        }
        
        // Set cooldown key with TTL
        redisService.setWithExpiry(key, "1", cooldownMinutes, TimeUnit.MINUTES);
        log.debug("Cooldown set for bot {} and user {} for {} minutes", 
            botId, humanId, cooldownMinutes);
    }
    
    /**
     * Rollback horizontal cap increment (used when transaction fails)
     */
    public void rollbackHorizontalCap(Long postId) {
        String key = String.format("post:%d:bot_count", postId);
        redisService.incrementBy(key, -1);
        log.debug("Rolled back bot count for post {}", postId);
    }
}
