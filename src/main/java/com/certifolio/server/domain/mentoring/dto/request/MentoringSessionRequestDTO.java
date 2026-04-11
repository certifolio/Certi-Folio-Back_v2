package com.certifolio.server.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MentoringSessionRequestDTO {

    public record CreateSessionRequest(
            @NotNull Long mentorId,
            @NotBlank String topic
    ) {}

    public record UpdateSessionStatusRequest(
            @NotBlank String status
    ) {}
}
