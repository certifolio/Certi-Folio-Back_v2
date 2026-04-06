package com.certifolio.server.domain.notification.controller;

import com.certifolio.server.domain.notification.dto.response.NotificationResponseDTO;
import com.certifolio.server.domain.notification.dto.response.NotificationScrollResponseDTO;
import com.certifolio.server.domain.notification.dto.response.RecentNotificationsResponseDTO;
import com.certifolio.server.domain.notification.entity.NotificationType;
import com.certifolio.server.domain.notification.service.NotificationService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알람 페이지 전체 조회
    @GetMapping
    public ApiResponse<NotificationScrollResponseDTO> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int limit) {

        NotificationScrollResponseDTO response = notificationService.getNotifications(userId, type, cursorId, limit);
        return ApiResponse.onSuccess("알람 전체 조회 성공", response);
    }

    // 드롭다운 알람 조회
    @GetMapping("/recent")
    public ApiResponse<RecentNotificationsResponseDTO> getRecentNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        RecentNotificationsResponseDTO response = notificationService.getRecentNotifications(userId);
        return ApiResponse.onSuccess("드롭다운 알람 조회 성공", response);
    }

    // 단일 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponseDTO> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        NotificationResponseDTO response = notificationService.markAsRead(userId, id);
        return ApiResponse.onSuccess("단일 알림 읽음 처리 성공", response);
    }

    // 전체 알림 읽음 처리
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal Long userId
    ) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.onSuccess("전체 알림 읽음 처리 성공");
    }

    // 단일 알림 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(userId, id);
        return ApiResponse.onSuccess("단일 알림 삭제 성공");
    }

    // 전체 알림 삭제
    @DeleteMapping("/all")
    public ApiResponse<Void> deleteAllNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        notificationService.deleteAllNotifications(userId);
        return ApiResponse.onSuccess("전체 알림 삭제 성공");

    }
}
