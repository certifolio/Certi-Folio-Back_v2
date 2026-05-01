package com.certifolio.server.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageRequestDTO {

    public record SendRequest(
            Long chatRoomId,
            @NotBlank String content
    ) {}

    public record CreateRoomRequest(
            @NotNull Long mentorId,
            Long menteeUserId
    ) {}
}
