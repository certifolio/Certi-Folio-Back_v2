package com.certifolio.server.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentModifyRequestDTO(
        @NotBlank String content
) {
}
