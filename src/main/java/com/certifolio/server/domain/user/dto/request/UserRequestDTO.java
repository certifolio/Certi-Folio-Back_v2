package com.certifolio.server.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestDTO(
        @NotBlank String name,
        @NotBlank String companyType,
        @NotBlank String jobRole
){}
