package com.certifolio.server.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MentoringApplicationRequestDTO {

    public record CreateRequest(
            @NotNull Long mentorId,
            @NotBlank String topic,
            @NotBlank @Size(min = 50, message = "신청 내용은 50자 이상 작성해주세요.") String description
    ) {}

    public record RejectRequest(
            @NotBlank String reason
    ) {}
}
