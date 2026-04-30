package com.certifolio.server.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequestDTO(
        @NotNull Long postId,
        @NotBlank String content
) {}
