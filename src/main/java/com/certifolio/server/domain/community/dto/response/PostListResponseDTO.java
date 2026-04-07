package com.certifolio.server.domain.community.dto.response;

import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.entity.PostType;

import java.time.LocalDateTime;

public record PostListResponseDTO(
        Long id,
        String authorName,
        String title,
        PostType type,
        int viewCount,
        long commentCount,
        LocalDateTime createdAt
) {
    public static PostListResponseDTO from(Post post, long commentCount) {
        return new PostListResponseDTO(
                post.getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getType(),
                post.getViewCount(),
                commentCount,
                post.getCreatedAt()
        );
    }
}