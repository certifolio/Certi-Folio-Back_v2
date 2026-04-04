package com.certifolio.server.domain.mentoring.dto.request;

public class MentoringApplicationRequestDTO {

    public record CreateRequest(
            Long mentorId,
            String topic,
            String description
    ) {}

    public record RejectRequest(
            String reason
    ) {}
}
