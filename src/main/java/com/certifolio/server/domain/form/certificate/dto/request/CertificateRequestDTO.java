package com.certifolio.server.domain.form.certificate.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CertificateRequestDTO(
        @NotBlank String name,
        @NotBlank String type,
        String issuer,
        @NotBlank String issueDate,
        @NotBlank String score,
        String certificateNumber
) {}
