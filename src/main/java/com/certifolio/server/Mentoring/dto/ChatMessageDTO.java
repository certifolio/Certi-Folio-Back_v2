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
    @Setter
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
        private Long sequenceNumber; // 시퀀스 번호 (클라이언트 정렬용)
        private String deliveryStatus; // SENT, DELIVERED, READ
    }

    /**
     * 채팅 기록 응답 DTO
     */
    @Getter
    @Setter
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
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomResponse {
        private Long chatRoomId;
        private Long mentorId;
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
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomListResponse {
        private boolean success;
        private List<ChatRoomResponse> rooms;
        private int totalCount;
    }

    // ===== ACK / 동기화 관련 DTO =====

    /**
     * 클라이언트 → 서버: 메시지 수신 확인 (ACK) 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AckRequest {
        private Long chatRoomId;
        private Long messageId;
        private String senderSubject; // ACK 전송자 식별용
    }

    /**
     * 서버 → 클라이언트: ACK 처리 결과 응답
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AckResponse {
        private Long messageId;
        private String status; // "ACK_OK"
        private LocalDateTime acknowledgedAt;
    }

    /**
     * 재접속 시 누락 메시지 동기화 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncRequest {
        private Long lastSequenceNumber; // 클라이언트가 마지막으로 수신한 시퀀스 번호
    }
}
