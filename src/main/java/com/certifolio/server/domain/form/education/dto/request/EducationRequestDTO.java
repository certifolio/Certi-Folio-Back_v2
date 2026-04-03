package com.certifolio.server.domain.form.education.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EducationRequestDTO(
        @NotBlank String schoolName,
        @NotBlank String major,
        @NotBlank String degree,
        @NotBlank String status,
        @NotBlank String startDate,
        @NotBlank String endDate,
        Double gpa,
        Double maxGpa
) {}
