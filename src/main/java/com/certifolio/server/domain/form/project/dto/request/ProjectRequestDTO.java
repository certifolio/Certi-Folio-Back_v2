package com.certifolio.server.domain.form.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequestDTO(
        @NotBlank String name,
        @NotBlank String type,
        @NotBlank String role,
        @NotBlank String techStack,
        @NotBlank String description,
        String githubLink,
        String demoLink,
        String result,
        @NotBlank String startDate,
        @NotBlank String endDate
) {}
