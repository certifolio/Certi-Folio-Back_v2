package com.certifolio.server.domain.community.dto.response;

import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.entity.PostType;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDTO(
        Long id,
        String authorName,
        String title,
        String content,
        PostType type,
        int viewCount,
        LocalDateTime createdAt,
        List<CommentResponseDTO> comments
) {
    public static PostResponseDTO from(Post post, List<CommentResponseDTO> comments) {
        return new PostResponseDTO(
                post.getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getContent(),
                post.getType(),
                post.getViewCount(),
                post.getCreatedAt(),
                comments
        );
    }
}
