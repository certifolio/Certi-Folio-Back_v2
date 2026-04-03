package com.certifolio.server.domain.form.career.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CareerRequestDTO(
        @NotBlank String type,
        @NotBlank String company,
        String position,
        @NotBlank String startDate,
        @NotBlank String endDate,
        @NotBlank String description
) {}
