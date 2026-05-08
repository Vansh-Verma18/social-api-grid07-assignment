package com.grid07.socialapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotNull(message = "Author ID is required")
    private Long authorId;
    
    private Long parentCommentId;
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    @NotNull(message = "Depth level is required")
    private Integer depthLevel;
    
    @NotNull(message = "Is bot flag is required")
    private Boolean isBot;
}
