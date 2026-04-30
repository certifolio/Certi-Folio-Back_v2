package com.certifolio.server.domain.community.dto.response;

import com.certifolio.server.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
    public static CommentResponseDTO from(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}