package com.certifolio.server.domain.form.activity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ActivityRequestDTO(
        @NotBlank String name,
        @NotBlank String type,
        @NotBlank String role,
        @NotBlank String startMonth,
        @NotBlank String endMonth,
        @NotBlank String description,
        String result
) {}

