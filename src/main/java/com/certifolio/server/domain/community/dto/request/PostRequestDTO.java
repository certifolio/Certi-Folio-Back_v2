package com.certifolio.server.domain.community.dto.request;

import com.certifolio.server.domain.community.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequestDTO(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull PostType type
) {}
