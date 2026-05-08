package com.grid07.socialapi.controller;

import com.grid07.socialapi.dto.ApiResponse;
import com.grid07.socialapi.dto.CreateCommentRequest;
import com.grid07.socialapi.dto.CreatePostRequest;
import com.grid07.socialapi.dto.LikePostRequest;
import com.grid07.socialapi.entity.Comment;
import com.grid07.socialapi.entity.Post;
import com.grid07.socialapi.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    
    private final PostService postService;
    
    /**
     * Create a new post
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Post>> createPost(@Valid @RequestBody CreatePostRequest request) {
        log.info("Received request to create post by author: {}", request.getAuthorId());
        
        Post post = postService.createPost(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Post created successfully", post));
    }
    
    /**
     * Add a comment to a post
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Comment>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        
        log.info("Received request to add comment to post {} by author: {}", 
            postId, request.getAuthorId());
        
        Comment comment = postService.addComment(postId, request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Comment added successfully", comment));
    }
    
    /**
     * Like a post
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            @Valid @RequestBody LikePostRequest request) {
        
        log.info("Received request to like post {} by user: {}", postId, request.getUserId());
        
        postService.likePost(postId, request.getUserId());
        
        return ResponseEntity
            .ok(ApiResponse.success("Post liked successfully", null));
    }
}
