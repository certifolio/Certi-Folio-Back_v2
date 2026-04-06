package com.certifolio.server.domain.notification.dto.response;

import java.util.List;

public record NotificationScrollResponseDTO(
        List<NotificationResponseDTO> notifications,
        boolean hasNext,
        Long nextCursorId
) {
}
