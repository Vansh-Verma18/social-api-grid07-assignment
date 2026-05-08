package com.grid07.socialapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViralityService {
    
    private final RedisService redisService;
    
    private static final int BOT_REPLY_POINTS = 1;
    private static final int HUMAN_LIKE_POINTS = 20;
    private static final int HUMAN_COMMENT_POINTS = 50;
    
    /**
     * Increment virality score for bot reply
     */
    public void incrementForBotReply(Long postId) {
        String key = String.format("post:%d:virality_score", postId);
        Long newScore = redisService.incrementBy(key, BOT_REPLY_POINTS);
        log.debug("Post {} virality score updated to {} (+{} for bot reply)", 
            postId, newScore, BOT_REPLY_POINTS);
    }
    
    /**
     * Increment virality score for human like
     */
    public void incrementForHumanLike(Long postId) {
        String key = String.format("post:%d:virality_score", postId);
        Long newScore = redisService.incrementBy(key, HUMAN_LIKE_POINTS);
        log.debug("Post {} virality score updated to {} (+{} for human like)", 
            postId, newScore, HUMAN_LIKE_POINTS);
    }
    
    /**
     * Increment virality score for human comment
     */
    public void incrementForHumanComment(Long postId) {
        String key = String.format("post:%d:virality_score", postId);
        Long newScore = redisService.incrementBy(key, HUMAN_COMMENT_POINTS);
        log.debug("Post {} virality score updated to {} (+{} for human comment)", 
            postId, newScore, HUMAN_COMMENT_POINTS);
    }
    
    /**
     * Get current virality score
     */
    public Long getViralityScore(Long postId) {
        String key = String.format("post:%d:virality_score", postId);
        Object score = redisService.get(key);
        return score != null ? Long.parseLong(score.toString()) : 0L;
    }
}
