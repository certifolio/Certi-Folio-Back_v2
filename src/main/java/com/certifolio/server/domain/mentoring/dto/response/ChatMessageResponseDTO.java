package com.certifolio.server.domain.mentoring.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageResponseDTO {

    @Builder
    public record MessageResponse(
            Long id,
            Long chatRoomId,
            Long senderId,
            String senderName,
            String senderProfileImage,
            String content,
            String type,
            LocalDateTime sentAt,
            boolean isMine
    ) {}

    @Builder
    public record ChatHistoryResponse(
            boolean success,
            Long chatRoomId,
            List<MessageResponse> messages,
            int totalCount
    ) {}

    @Builder
    public record ChatRoomResponse(
            Long chatRoomId,
            Long mentorId,
            Long mentorUserId,
            String mentorName,
            String mentorProfileImage,
            String mentorCompany,
            Long userId,
            String userName,
            String userProfileImage,
            LocalDateTime createdAt,
            LocalDateTime lastMessageAt
    ) {}

    @Builder
    public record ChatRoomListResponse(
            boolean success,
            List<ChatRoomResponse> rooms,
            int totalCount
    ) {}
}
