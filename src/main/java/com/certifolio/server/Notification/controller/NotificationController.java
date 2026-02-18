package com.certifolio.server.Notification.controller;

import com.certifolio.server.Notification.dto.NotificationDTO;
import com.certifolio.server.Notification.service.NotificationService;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * 알림 목록 조회 (페이지네이션)
     * GET /api/notifications?page=1&limit=10&type=mentoring
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String type) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    NotificationDTO.SuccessResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        NotificationDTO.PaginatedNotificationResponse response =
                notificationService.getNotifications(user, type, page, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 최신 알림 조회 (네비바 드롭다운용)
     * GET /api/notifications/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    NotificationDTO.SuccessResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        NotificationDTO.RecentNotificationsResponse response =
                notificationService.getRecentNotifications(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 단일 알림 읽음 처리
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO.SuccessResponse> markAsRead(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    NotificationDTO.SuccessResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        NotificationDTO.SuccessResponse response = notificationService.markAsRead(user, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 알림 읽음 처리
     * PATCH /api/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<NotificationDTO.SuccessResponse> markAllAsRead(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    NotificationDTO.SuccessResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        NotificationDTO.SuccessResponse response = notificationService.markAllAsRead(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 알림 삭제
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<NotificationDTO.SuccessResponse> deleteNotification(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    NotificationDTO.SuccessResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        NotificationDTO.SuccessResponse response = notificationService.deleteNotification(user, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Principal에서 User 조회 (MentorController와 동일한 패턴)
     */
    private User getUser(Object principal) {
        String subject = null;
        if (principal instanceof UserDetails) {
            subject = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            subject = (String) principal;
        }

        if (subject == null) return null;

        // Token subject is always "provider:providerId" format
        if (subject.contains(":")) {
            String[] parts = subject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
        }

        return null;
    }
}
