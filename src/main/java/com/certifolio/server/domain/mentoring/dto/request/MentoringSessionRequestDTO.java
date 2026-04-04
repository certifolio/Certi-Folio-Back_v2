package com.certifolio.server.domain.mentoring.dto.request;

public class MentoringSessionRequestDTO {

    public record CreateSessionRequest(
            Long mentorId,
            String topic
    ) {}

    public record UpdateSessionStatusRequest(
            String status
    ) {}
}
