package com.certifolio.server.domain.notification.dto.response;

import java.util.List;

public record RecentNotificationsResponseDTO(
        List<NotificationResponseDTO> notifications,
        int unreadCount
) {
}
