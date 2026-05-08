package com.grid07.socialapi.service;

import com.grid07.socialapi.dto.CreateCommentRequest;
import com.grid07.socialapi.dto.CreatePostRequest;
import com.grid07.socialapi.entity.Bot;
import com.grid07.socialapi.entity.Comment;
import com.grid07.socialapi.entity.Post;
import com.grid07.socialapi.entity.User;
import com.grid07.socialapi.repository.BotRepository;
import com.grid07.socialapi.repository.CommentRepository;
import com.grid07.socialapi.repository.PostRepository;
import com.grid07.socialapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final GuardrailService guardrailService;
    private final ViralityService viralityService;
    private final NotificationService notificationService;
    
    /**
     * Create a new post
     */
    @Transactional
    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setContent(request.getContent());
        
        Post savedPost = postRepository.save(post);
        log.info("Created post with ID: {}", savedPost.getId());
        
        return savedPost;
    }
    
    /**
     * Add a comment to a post with guardrail checks
     */
    @Transactional
    public Comment addComment(Long postId, CreateCommentRequest request) {
        // Verify post exists
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        
        // Check vertical cap first (no Redis operation needed)
        guardrailService.checkVerticalCap(request.getDepthLevel());
        
        // If this is a bot comment, apply all bot-specific guardrails
        if (Boolean.TRUE.equals(request.getIsBot())) {
            try {
                // Check horizontal cap (atomic Redis operation)
                guardrailService.checkHorizontalCap(postId);
                
                // Determine the human being interacted with
                Long targetHumanId = determineTargetHuman(post, request.getParentCommentId());
                
                // Check cooldown cap only if target is a human (not null)
                if (targetHumanId != null) {
                    guardrailService.checkCooldownCap(request.getAuthorId(), targetHumanId);
                }
                
                // All guardrails passed - save comment
                Comment comment = saveComment(postId, request);
                
                // Update virality score for bot reply
                viralityService.incrementForBotReply(postId);
                
                // Handle notification for bot interaction (only if target is human)
                if (targetHumanId != null) {
                    Bot bot = botRepository.findById(request.getAuthorId())
                        .orElseThrow(() -> new RuntimeException("Bot not found"));
                    notificationService.handleBotInteraction(targetHumanId, bot.getName(), "replied to");
                }
                
                log.info("Bot {} added comment to post {}", request.getAuthorId(), postId);
                return comment;
                
            } catch (Exception e) {
                // If any error occurs after horizontal cap increment, rollback
                guardrailService.rollbackHorizontalCap(postId);
                throw e;
            }
        } else {
            // Human comment - no guardrails, just save and update virality
            Comment comment = saveComment(postId, request);
            viralityService.incrementForHumanComment(postId);
            
            log.info("User {} added comment to post {}", request.getAuthorId(), postId);
            return comment;
        }
    }
    
    /**
     * Determine the target human for cooldown check
     * Returns the human ID if the bot is interacting with a human's content, null otherwise
     */
    private Long determineTargetHuman(Post post, Long parentCommentId) {
        // If replying to a comment, check the comment author
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            
            // Check if parent comment author is a human (exists in User table)
            if (userRepository.existsById(parentComment.getAuthorId())) {
                return parentComment.getAuthorId();
            }
            // If parent is a bot, no cooldown needed
            return null;
        }
        
        // If replying directly to post, check post author
        if (userRepository.existsById(post.getAuthorId())) {
            return post.getAuthorId();
        }
        
        // Post author is a bot, no cooldown needed
        return null;
    }
    
    /**
     * Like a post
     */
    @Transactional
    public void likePost(Long postId, Long userId) {
        // Verify post exists
        postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Update virality score for human like
        viralityService.incrementForHumanLike(postId);
        
        log.info("User {} liked post {}", userId, postId);
    }
    
    /**
     * Helper method to save comment
     */
    private Comment saveComment(Long postId, CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.getAuthorId());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());
        
        return commentRepository.save(comment);
    }
}
