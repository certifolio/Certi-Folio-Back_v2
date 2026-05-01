package com.certifolio.server.domain.groupchat.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class GroupChatResponseDTO {

    @Builder
    public record RoomResponse(
            Long roomId,
            String chattingroomName,
            String description,
            long participantCount,
            boolean joined,
            LocalDateTime createdAt,
            LocalDateTime lastMessageAt
    ) {}

    @Builder
    public record RoomListResponse(
            List<RoomResponse> rooms,
            int totalCount
    ) {}

    @Builder
    public record MessageResponse(
            Long id,
            Long roomId,
            Long senderId,
            String senderName,
            String senderProfileImage,
            String content,
            String type,
            LocalDateTime sentAt,
            boolean isMine
    ) {}

    @Builder
    public record MessageHistoryResponse(
            Long roomId,
            List<MessageResponse> messages,
            int size,
            Long nextCursor,
            boolean hasNext
    ) {}

    @Builder
    public record JoinResponse(
            Long roomId,
            String chattingroomName,
            long participantCount,
            LocalDateTime joinedAt
    ) {}

    @Builder
    public record JoinResult(
            JoinResponse joinResponse,
            MessageResponse systemMessage
    ) {}
}
