package com.certifolio.server.Notification.repository;

import com.certifolio.server.Notification.domain.Notification;
import com.certifolio.server.Notification.domain.NotificationType;
import com.certifolio.server.User.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 유저의 전체 알림 조회 (최신순, 페이지네이션)
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 유저의 알림을 타입별 필터링 조회 (최신순, 페이지네이션)
     */
    Page<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type, Pageable pageable);

    /**
     * 유저의 읽지 않은 알림 수
     */
    int countByUserAndIsReadFalse(User user);

    /**
     * 유저의 최신 알림 N개 조회 (네비바 드롭다운용)
     */
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);

    /**
     * 유저의 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadByUser(@Param("user") User user);
}
