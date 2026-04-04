package com.certifolio.server.Mentoring.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 관련 DTO (Java Record)
 */
public class ChatMessageDTO {

    /**
     * WebSocket으로 메시지 전송 시 요청 DTO
     */
    public record SendRequest(
            Long chatRoomId,
            String content,
            String senderSubject
    ) {}

    /**
     * 채팅방 생성/조회 요청 DTO
     */
    public record CreateRoomRequest(
            Long mentorId,
            Long userId
    ) {}

    /**
     * 개별 메시지 응답 DTO
     */
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

    /**
     * 채팅 기록 응답 DTO
     */
    @Builder
    public record ChatHistoryResponse(
            boolean success,
            Long chatRoomId,
            List<MessageResponse> messages,
            int totalCount
    ) {}

    /**
     * 채팅방 정보 응답 DTO
     */
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

    /**
     * 내 채팅방 목록 응답 DTO
     */
    @Builder
    public record ChatRoomListResponse(
            boolean success,
            List<ChatRoomResponse> rooms,
            int totalCount
    ) {}
}
