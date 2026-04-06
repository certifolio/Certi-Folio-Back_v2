package com.certifolio.server.domain.notification.service;

import com.certifolio.server.domain.notification.dto.response.NotificationResponseDTO;
import com.certifolio.server.domain.notification.dto.response.NotificationScrollResponseDTO;
import com.certifolio.server.domain.notification.dto.response.RecentNotificationsResponseDTO;
import com.certifolio.server.domain.notification.entity.Notification;
import com.certifolio.server.domain.notification.entity.NotificationType;
import com.certifolio.server.domain.notification.repository.NotificationRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    // 알림 생성
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

    // 알림 목록 조회 (알림 페이지)
    @Transactional(readOnly = true)
    public NotificationScrollResponseDTO getNotifications(Long userId, NotificationType type, Long cursorId, int limit) {
        User user = userService.getUserById(userId);
        if (cursorId == null) cursorId = Long.MAX_VALUE;

        List<Notification> notifications = (type != null)
                ? notificationRepository.findByUserAndTypeAndIdLessThan(user, type, cursorId, limit + 1)
                : notificationRepository.findByUserAndIdLessThan(user, cursorId, limit + 1);

        boolean hasNext = notifications.size() > limit;
        List<Notification> content = hasNext
                ? notifications.subList(0, limit)
                : notifications;

        List<NotificationResponseDTO> response = content.stream()
                .map(NotificationResponseDTO::from)
                .toList();

        Long nextCursorId = hasNext ? content.get(content.size() - 1).getId() : null;

        return new NotificationScrollResponseDTO(response, hasNext, nextCursorId);
    }


    // 최신 알림 조회 (네비게이션 드롭다운 메뉴용)
    @Transactional(readOnly = true)
    public RecentNotificationsResponseDTO getRecentNotifications(Long userId) {
        User user = userService.getUserById(userId);
        List<Notification> recent = notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        int unreadCount = notificationRepository.countByUserAndIsReadFalse(user);

        List<NotificationResponseDTO> notifications = recent.stream()
                .map(NotificationResponseDTO::from)
                .toList();

        return new RecentNotificationsResponseDTO(notifications, unreadCount);
    }

    // 단일 알림 읽음 처리
    @Transactional
    public NotificationResponseDTO markAsRead(Long userId, Long notificationId) {
        User user = userService.getUserById(userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(GeneralErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notification.markAsRead();

        return NotificationResponseDTO.from(notification);
    }

    // 전체 알림 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userService.getUserById(userId);
        notificationRepository.markAllAsReadByUser(user);
    }

    // 단일 알림 삭제
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        User user = userService.getUserById(userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(GeneralErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notificationRepository.delete(notification);
    }

    // 알림 전체 삭제
    @Transactional
    public void deleteAllNotifications(Long userId) {
        User user = userService.getUserById(userId);

        notificationRepository.deleteAllByUser(user);
    }



}
