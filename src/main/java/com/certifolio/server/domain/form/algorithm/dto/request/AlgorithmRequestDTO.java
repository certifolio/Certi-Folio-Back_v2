package com.certifolio.server.domain.form.algorithm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AlgorithmRequestDTO(
        @NotBlank String bojHandle
) {}
