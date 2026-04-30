package com.certifolio.server.domain.notification.dto.response;

import com.certifolio.server.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public record NotificationResponseDTO(
        Long id,
        String type,
        String title,
        String message,
        String timestamp,
        boolean isRead,
        String actionUrl
) {
    public static NotificationResponseDTO from(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType().toFrontendValue(),
                notification.getTitle(),
                notification.getMessage(),
                formatTimestamp(notification.getCreatedAt()),
                notification.isRead(),
                notification.getActionUrl()
        );
    }

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
