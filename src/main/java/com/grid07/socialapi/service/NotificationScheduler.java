package com.grid07.socialapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    
    private final RedisService redisService;
    
    /**
     * Scheduled task that runs every 5 minutes to sweep pending notifications
     * and send summarized push notifications to users
     */
    @Scheduled(cron = "${app.scheduler.notification-sweep-cron}")
    public void sweepPendingNotifications() {
        log.info("Starting notification sweep...");
        
        try {
            // Find all users with pending notifications
            Set<String> pendingKeys = redisService.keys("user:*:pending_notifs");
            
            if (pendingKeys == null || pendingKeys.isEmpty()) {
                log.info("No pending notifications found");
                return;
            }
            
            int processedUsers = 0;
            int totalNotifications = 0;
            
            for (String key : pendingKeys) {
                try {
                    // Extract user ID from key (format: user:{userId}:pending_notifs)
                    String userId = key.split(":")[1];
                    
                    // Get all pending notifications for this user
                    List<Object> notifications = redisService.getList(key);
                    
                    if (notifications != null && !notifications.isEmpty()) {
                        int count = notifications.size();
                        totalNotifications += count;
                        
                        // Extract first bot name from first notification
                        String firstNotif = notifications.get(0).toString();
                        String botName = extractBotName(firstNotif);
                        
                        // Log summarized notification
                        if (count == 1) {
                            log.info("Summarized Push Notification: {} interacted with your posts.", 
                                botName);
                        } else {
                            log.info("Summarized Push Notification: {} and {} others interacted with your posts.", 
                                botName, count - 1);
                        }
                        
                        // Clear the pending notifications list
                        redisService.delete(key);
                        processedUsers++;
                    }
                } catch (Exception e) {
                    log.error("Error processing notifications for key {}: {}", key, e.getMessage());
                }
            }
            
            log.info("Notification sweep completed: {} users processed, {} notifications sent", 
                processedUsers, totalNotifications);
            
        } catch (Exception e) {
            log.error("Error during notification sweep: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Extract bot name from notification message
     * Expected format: "Bot {name} {action} your post"
     */
    private String extractBotName(String notification) {
        try {
            if (notification.startsWith("Bot ")) {
                String[] parts = notification.split(" ");
                if (parts.length >= 2) {
                    return "Bot " + parts[1];
                }
            }
            return "Bot X";
        } catch (Exception e) {
            return "Bot X";
        }
    }
}
