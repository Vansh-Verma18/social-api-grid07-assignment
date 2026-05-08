package com.grid07.socialapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final RedisService redisService;
    
    @Value("${app.redis.notification-cooldown-minutes:15}")
    private int notificationCooldownMinutes;
    
    /**
     * Handle notification for bot interaction with user's post
     * Implements smart batching to prevent notification spam
     */
    public void handleBotInteraction(Long userId, String botName, String action) {
        String cooldownKey = String.format("user:%d:notif_cooldown", userId);
        String pendingKey = String.format("user:%d:pending_notifs", userId);
        
        // Check if user has received a notification recently
        if (Boolean.TRUE.equals(redisService.exists(cooldownKey))) {
            // User is in cooldown period - batch the notification
            String message = String.format("Bot %s %s your post", botName, action);
            redisService.pushToList(pendingKey, message);
            log.debug("Notification batched for user {}: {}", userId, message);
        } else {
            // No cooldown - send immediate notification
            log.info("Push Notification Sent to User {}: Bot {} {} your post", 
                userId, botName, action);
            
            // Set cooldown for this user
            redisService.setWithExpiry(cooldownKey, "1", 
                notificationCooldownMinutes, TimeUnit.MINUTES);
            log.debug("Notification cooldown set for user {} for {} minutes", 
                userId, notificationCooldownMinutes);
        }
    }
}
