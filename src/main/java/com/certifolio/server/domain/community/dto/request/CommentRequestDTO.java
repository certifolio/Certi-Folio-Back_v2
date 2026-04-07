package com.certifolio.server.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequestDTO(
        @NotNull Long postId,
        @NotBlank String content
) {}
