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
        private Long sessionId;
        private String content;
        private String senderSubject; // JWT subject (provider:providerId)
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
        private Long sessionId;
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
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatHistoryResponse {
        private boolean success;
        private Long sessionId;
        private List<MessageResponse> messages;
        private int totalCount;
    }
}
