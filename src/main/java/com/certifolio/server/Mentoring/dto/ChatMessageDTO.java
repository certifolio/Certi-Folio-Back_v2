package com.certifolio.server.Mentoring.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageDTO {

    /**
     * WebSocket으로 메시지 전송 시 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendRequest {
        private Long chatRoomId;
        private String content;
        private String senderSubject; // JWT subject (provider:providerId)
    }

    /**
     * 채팅방 생성/조회 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRoomRequest {
        private Long mentorId;
        private Long userId; // 멘토가 채팅방을 만들 때 멘티의 userId 지정
    }

    /**
     * 개별 메시지 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResponse {
        private Long id;
        private Long chatRoomId;
        private Long senderId;
        private String senderName;
        private String senderProfileImage;
        private String content;
        private String type; // TEXT, SYSTEM
        private LocalDateTime sentAt;
        private boolean isMine; // 본인 메시지 여부 (REST 조회 시 사용)
    }

    /**
     * 채팅 기록 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatHistoryResponse {
        private boolean success;
        private Long chatRoomId;
        private List<MessageResponse> messages;
        private int totalCount;
    }

    /**
     * 채팅방 정보 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomResponse {
        private Long chatRoomId;
        private Long mentorId;
        private Long mentorUserId; // 멘토의 User 테이블 PK (프론트엔드 역할 비교용)
        private String mentorName;
        private String mentorProfileImage;
        private String mentorCompany;
        private Long userId;
        private String userName;
        private String userProfileImage;
        private LocalDateTime createdAt;
        private LocalDateTime lastMessageAt;
    }

    /**
     * 내 채팅방 목록 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomListResponse {
        private boolean success;
        private List<ChatRoomResponse> rooms;
        private int totalCount;
    }
}
