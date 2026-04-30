package com.certifolio.server.domain.notification.repository;

import com.certifolio.server.domain.notification.entity.Notification;
import com.certifolio.server.domain.notification.entity.NotificationType;
import com.certifolio.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.id < :cursorId ORDER BY n.id DESC LIMIT :limit")
    List<Notification> findByUserAndIdLessThan(@Param("user") User user, @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type AND n.id < :cursorId ORDER BY n.id DESC LIMIT :limit")
    List<Notification> findByUserAndTypeAndIdLessThan(@Param("user") User user, @Param("type") NotificationType type, @Param("cursorId") Long cursorId, @Param("limit") int limit);

    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);

    int countByUserAndIsReadFalse(User user);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);

    void deleteAllByUser(User user);


}
