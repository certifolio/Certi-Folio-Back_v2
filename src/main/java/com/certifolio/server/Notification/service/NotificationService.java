package com.certifolio.server.Notification.service;

import com.certifolio.server.Notification.domain.Notification;
import com.certifolio.server.Notification.domain.NotificationType;
import com.certifolio.server.Notification.dto.NotificationDTO;
import com.certifolio.server.Notification.repository.NotificationRepository;
import com.certifolio.server.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 목록 조회 (페이지네이션 + 타입 필터링)
     */
    public NotificationDTO.PaginatedNotificationResponse getNotifications(User user, String type, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit); // 프론트는 1-indexed, Spring은 0-indexed
        Page<Notification> notificationPage;

        NotificationType notificationType = NotificationType.fromFrontendValue(type);

        if (notificationType != null) {
            notificationPage = notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, notificationType, pageable);
        } else {
            notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<NotificationDTO.NotificationResponse> items = notificationPage.getContent().stream()
                .map(NotificationDTO.NotificationResponse::from)
                .collect(Collectors.toList());

        NotificationDTO.PaginationMeta meta = NotificationDTO.PaginationMeta.builder()
                .total(notificationPage.getTotalElements())
                .page(page)
                .limit(limit)
                .totalPages(notificationPage.getTotalPages())
                .hasNext(notificationPage.hasNext())
                .hasPrev(notificationPage.hasPrevious())
                .build();

        return NotificationDTO.PaginatedNotificationResponse.builder()
                .items(items)
                .meta(meta)
                .build();
    }

    /**
     * 최신 알림 조회 (네비바 드롭다운용)
     */
    public NotificationDTO.RecentNotificationsResponse getRecentNotifications(User user) {
        List<Notification> recent = notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        int unreadCount = notificationRepository.countByUserAndIsReadFalse(user);

        List<NotificationDTO.NotificationResponse> notifications = recent.stream()
                .map(NotificationDTO.NotificationResponse::from)
                .collect(Collectors.toList());

        return NotificationDTO.RecentNotificationsResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 단일 알림 읽음 처리
     */
    @Transactional
    public NotificationDTO.SuccessResponse markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        if (notification == null) {
            return NotificationDTO.SuccessResponse.builder()
                    .success(false)
                    .message("알림을 찾을 수 없습니다.")
                    .build();
        }

        // 본인의 알림만 읽음 처리 가능
        if (!notification.getUser().getId().equals(user.getId())) {
            return NotificationDTO.SuccessResponse.builder()
                    .success(false)
                    .message("권한이 없습니다.")
                    .build();
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        return NotificationDTO.SuccessResponse.builder()
                .success(true)
                .message("알림을 읽음 처리했습니다.")
                .build();
    }

    /**
     * 전체 알림 읽음 처리
     */
    @Transactional
    public NotificationDTO.SuccessResponse markAllAsRead(User user) {
        int updatedCount = notificationRepository.markAllAsReadByUser(user);

        return NotificationDTO.SuccessResponse.builder()
                .success(true)
                .message("모든 알림을 읽음 처리했습니다.")
                .updatedCount(updatedCount)
                .build();
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public NotificationDTO.SuccessResponse deleteNotification(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);

        if (notification == null) {
            return NotificationDTO.SuccessResponse.builder()
                    .success(false)
                    .message("알림을 찾을 수 없습니다.")
                    .build();
        }

        // 본인의 알림만 삭제 가능
        if (!notification.getUser().getId().equals(user.getId())) {
            return NotificationDTO.SuccessResponse.builder()
                    .success(false)
                    .message("권한이 없습니다.")
                    .build();
        }

        notificationRepository.delete(notification);

        return NotificationDTO.SuccessResponse.builder()
                .success(true)
                .message("알림을 삭제했습니다.")
                .build();
    }

    /**
     * 알림 생성 (다른 서비스에서 호출)
     * 예: 멘토링 신청 시, 멘토에게 알림 전송
     */
    @Transactional
    public Notification createNotification(User user, NotificationType type, String title, String message, String actionUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .build();

        return notificationRepository.save(notification);
    }
}
