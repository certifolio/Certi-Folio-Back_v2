package com.certifolio.server.Notification.dto;

import com.certifolio.server.Notification.domain.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 알림 관련 DTO
 */
public class NotificationDTO {

    /**
     * 단일 알림 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private String id;
        private String type;
        private String title;
        private String message;
        private String timestamp;
        @JsonProperty("isRead")
        private boolean isRead;
        private String actionUrl;

        public static NotificationResponse from(Notification notification) {
            return NotificationResponse.builder()
                    .id(String.valueOf(notification.getId()))
                    .type(notification.getType().toFrontendValue())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .timestamp(formatTimestamp(notification.getCreatedAt()))
                    .isRead(notification.isRead())
                    .actionUrl(notification.getActionUrl())
                    .build();
        }

        /**
         * 상대적 시간 표시 (5분 전, 1시간 전, 3일 전 등)
         */
        private static String formatTimestamp(LocalDateTime dateTime) {
            if (dateTime == null) return "";

            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(dateTime, now);
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            long days = ChronoUnit.DAYS.between(dateTime, now);

            if (minutes < 1) return "방금 전";
            if (minutes < 60) return minutes + "분 전";
            if (hours < 24) return hours + "시간 전";
            if (days < 7) return days + "일 전";

            return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }

    /**
     * 페이지네이션된 알림 목록 응답
     * 프론트엔드 PaginatedResponse<T> 형식에 맞춤
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedNotificationResponse {
        private List<NotificationResponse> items;
        private PaginationMeta meta;
    }

    /**
     * 페이지네이션 메타데이터
     * 프론트엔드 PaginationMeta 형식에 맞춤
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMeta {
        private long total;
        private int page;
        private int limit;

        @JsonProperty("total_pages")
        private int totalPages;

        @JsonProperty("has_next")
        private boolean hasNext;

        @JsonProperty("has_prev")
        private boolean hasPrev;
    }

    /**
     * 네비바 드롭다운용 최신 알림 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentNotificationsResponse {
        private List<NotificationResponse> notifications;
        private int unreadCount;
    }

    /**
     * 성공/실패 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessResponse {
        private boolean success;
        private String message;
        @Builder.Default
        private int updatedCount = 0;
    }
}
