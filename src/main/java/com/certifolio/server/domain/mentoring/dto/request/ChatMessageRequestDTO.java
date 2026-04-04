package com.certifolio.server.domain.mentoring.dto.request;

public class ChatMessageRequestDTO {

    public record SendRequest(
            Long chatRoomId,
            String content,
            String senderSubject
    ) {}

    public record CreateRoomRequest(
            Long mentorId,
            Long userId
    ) {}
}
